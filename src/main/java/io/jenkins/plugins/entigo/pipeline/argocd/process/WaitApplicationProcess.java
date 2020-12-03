package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.glassfish.jersey.client.ChunkedInput;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.StringJoiner;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-04
 */
public class WaitApplicationProcess extends RequestProcess<Void> {

    private static final Integer READ_TIMEOUT = 30000;

    private final TaskListener listener;
    private final ArgoCDClient argoCDClient;
    private final String applicationName;
    private String lastLoggedMessage;
    private transient Response response = null;
    private transient ChunkedInput<ApplicationWatchEvent> input = null;

    public WaitApplicationProcess(TaskListener listener, ArgoCDClient argoCDClient, String applicationName) {
        super(listener);
        this.listener = listener;
        this.argoCDClient = argoCDClient;
        this.applicationName = applicationName;
    }

    protected ProcessResult<Void> run() throws AbortException {
        try {
            input = getInput();
            ApplicationWatchEvent event;
            while ((event = input.read()) != null && isRunning()) {
                checkInterruptions();
                resetRetryDelay();
                if (isApplicationReady(event.getResult().getApplication())) {
                    ListenerUtil.println(listener, "Application is synced and healthy");
                    return ProcessResult.success(null);
                }
            }
            // Null event, either client or server closed the connection
            if (getRetryDelay() > INITIAL_RETRY_DELAY) {
                ListenerUtil.println(listener, String.format("Connection was interrupted, retrying in %d seconds",
                        getRetryDelay()));
            }
        } finally {
            close();
        }
        return ProcessResult.unfinished();
    }

    private ChunkedInput<ApplicationWatchEvent> getInput() {
        // Using timeout because otherwise closing a blocking InputStream would be only possible when the block releases
        this.response = argoCDClient.watchApplication(applicationName, READ_TIMEOUT);
        ChunkedInput<ApplicationWatchEvent> chunkedInput = response.readEntity(
                new GenericType<ChunkedInput<ApplicationWatchEvent>>() {}
        );
        chunkedInput.setParser(ChunkedInput.createParser("\n"));
        return chunkedInput;
    }

    @Override
    protected synchronized void close() {
        if (input != null) {
            input.close();
        }
        if (response != null) {
            response.close();
        }
    }

    // Base logic imported from the official ArgoCD CLI wait command src app.go method waitOnApplicationStatus
    private boolean isApplicationReady(Application application) throws AbortException {
        if (application.getOperation() != null) {
            return false;
        } else if (application.getStatus().getOperationState() != null) {
            ApplicationStatus status = application.getStatus();
            OperationState operationState = status.getOperationState();
            if (OperationPhase.FAILED.getPhase().equals(operationState.getPhase())) {
                failProcess(operationState);
            }  else if (operationState.getFinishedAt() == null || (status.getReconciledAt() == null ||
                    status.getReconciledAt().isBefore(operationState.getFinishedAt()))) {
                logMessage(getStatus(application, true));
                return false;
            }
        }

        String healthStatus = application.getStatus().getHealth().getStatus();
        String syncStatus = application.getStatus().getSync().getStatus();
        logMessage(getStatus(application, false));
        return Health.HEALTHY.getStatus().equals(healthStatus) && Sync.SYNCED.getStatus().equals(syncStatus);
    }

    private void failProcess(OperationState operationState) throws AbortException {
        ListenerUtil.error(listener, "ArgoCD operation failed with message: " + operationState.getMessage());
        logSyncFailures(operationState.getSyncResult());
        throw new AbortException("Application failed to reach synced and ready status");
    }

    private void logSyncFailures(OperationResult result) {
        if (result != null && result.getResources() != null) {
            for (ResourceResult resource : result.getResources()) {
                if (Sync.SYNC_FAILED.getStatus().equals(resource.getStatus())) {
                    ListenerUtil.error(listener, String.format("%s (%s) - %s, %s", resource.getName(),
                            resource.getKind(), resource.getStatus(), resource.getMessage()));
                }
            }
        }
    }

    private void logMessage(String message) {
        if (!message.equals(lastLoggedMessage)) {
            ListenerUtil.println(listener, message);
            lastLoggedMessage = message;
        }
    }

    private String getStatus(Application application, boolean operationInProgress) {
        StringBuilder sb = new StringBuilder();
        if (operationInProgress) {
            sb.append("Operation in progress");
        } else {
            sb.append("Operation finished");
        }
        sb.append(", resource statuses: ");
        sb.append(getResourceStatuses(application.getStatus().getResources()));
        return sb.toString();
    }

    private String getResourceStatuses(List<ResourceStatus> resources) {
        if (resources == null || resources.isEmpty()) {
            return "no resources found";
        } else {
            StringJoiner sb = new StringJoiner("; ");
            for (ResourceStatus resource : resources) {
                if (!Sync.SYNCED.getStatus().equals(resource.getStatus())) {
                    sb.add(getResourceStatus(resource, resource.getStatus()));
                } else if (resource.getHealth() != null &&
                        !Health.HEALTHY.getStatus().equals(resource.getHealth().getStatus())) {
                    sb.add(getResourceStatus(resource, resource.getHealth().getStatus()));
                }
            }
            if (sb.length() == 0) {
                return "all ready";
            } else {
                return sb.toString();
            }
        }
    }

    private String getResourceStatus(ResourceStatus resource, String status) {
        return String.format("%s (%s) - %s", resource.getName(), resource.getKind(), status);
    }
}
