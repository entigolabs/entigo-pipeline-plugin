package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.glassfish.jersey.client.ChunkedInput;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.StringJoiner;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-04
 */
public class ArgoCDWaitProcess extends AbstractProcess {

    private static final int INITIAL_RETRY_DELAY = 1;
    private static final int MAX_RETRY_DELAY = 30;
    private static final Integer READ_TIMEOUT = 30000;

    private final TaskListener listener;
    private final ArgoCDClient argoCDClient;
    private final String applicationName;
    private boolean wait = true;
    private String lastLoggedMessage;
    private transient Response response = null;
    private transient ChunkedInput<ApplicationWatchEvent> input = null;

    public ArgoCDWaitProcess(StepContext context, TaskListener listener, ArgoCDClient argoCDClient,
                             String applicationName) {
        super(context);
        this.listener = listener;
        this.argoCDClient = argoCDClient;
        this.applicationName = applicationName;
    }

    @Override
    public void start() {
        long retryDelay = INITIAL_RETRY_DELAY;
        while (wait) {
            try {
                input = getInput();
                ApplicationWatchEvent event;
                while ((event = input.read()) != null && wait) {
                    if (Thread.currentThread().isInterrupted()) {
                        ListenerUtil.println(listener, "Process was interrupted, stopping process");
                        return;
                    }
                    retryDelay = INITIAL_RETRY_DELAY;
                    if (isApplicationReady(event.getResult().getApplication())) {
                        ListenerUtil.println(listener, "Application is synced and healthy");
                        success(null);
                        return;
                    }
                }
                // Null event, either client or server closed the connection
                if (retryDelay > INITIAL_RETRY_DELAY) {
                    ListenerUtil.println(listener, String.format("Connection was interrupted, retrying in %d seconds",
                            retryDelay));
                }
            } catch (ResponseException exception) {
                ListenerUtil.println(listener, String.format("Request failed, retrying in %d seconds," +
                        " exception message: %s", retryDelay, exception.getMessage()));
            } finally {
                close();
            }
            try {
                // TODO Should we use a jenkins Timer or a Task Scheduler for waiting?
                Thread.sleep(retryDelay * 1000L);
            } catch (InterruptedException e) {
                if (wait) {
                    ListenerUtil.println(listener, "Process was interrupted, stopping process");
                    wait = false;
                }
            }
            if (retryDelay < MAX_RETRY_DELAY) {
                retryDelay = Math.min(MAX_RETRY_DELAY, retryDelay * 2);
            }
        }
        ListenerUtil.error(listener, "Application failed to reach synced and healthy status");
    }

    @Override
    public void stop() {
        this.wait = false;
        ListenerUtil.println(listener, "Stopping the process can take up to " + READ_TIMEOUT / 1000 + " seconds");
        close();
    }

    private ChunkedInput<ApplicationWatchEvent> getInput() {
        // Using timeout because otherwise closing a blocking InputStream would be only possible when the block releases
        this.response = argoCDClient.watchApplication(applicationName, READ_TIMEOUT);
        ChunkedInput<ApplicationWatchEvent> input = response.readEntity(
                new GenericType<ChunkedInput<ApplicationWatchEvent>>() {}
        );
        input.setParser(ChunkedInput.createParser("\n"));
        return input;
    }

    private void close() {
        if (input != null) {
            input.close();
        }
        if (response != null) {
            response.close();
        }
    }

    // Base logic imported from the official ArgoCD CLI wait command src app.go method waitOnApplicationStatus
    private boolean isApplicationReady(Application application) {
        if (application.getOperation() != null) {
            return false;
        } else if (application.getStatus().getOperationState() != null) {
            ApplicationStatus status = application.getStatus();
            if (status.getOperationState().getFinishedAt() == null || (status.getReconciledAt() == null ||
                    status.getReconciledAt().isBefore(status.getOperationState().getFinishedAt()))) {
                logMessage(getStatus(application, true));
                return false;
            }
        }

        String healthStatus = application.getStatus().getHealth().getStatus();
        String syncStatus = application.getStatus().getSync().getStatus();
        logMessage(getStatus(application, false));
        return Health.HEALTHY.getStatus().equals(healthStatus) && Sync.SYNCED.getStatus().equals(syncStatus);
    }

    private void logMessage(String message) {
        if (!message.equals(lastLoggedMessage)) {
            ListenerUtil.println(listener, message);
            lastLoggedMessage = message;
        }
    }

    private String getStatus(Application application, Boolean operationInProgress) {
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
        if (resources == null || resources.size() == 0) {
            return "no resources found";
        } else {
            StringJoiner sb = new StringJoiner("; ");
            for (ResourceStatus resource : resources) {
                if (!Sync.SYNCED.getStatus().equals(resource.getStatus())) {
                    sb.add(resource.getName() + " - out of sync");
                } else {
                    String status;
                    if (resource.getHealth() == null) {
                        status = Health.UNKNOWN.getStatus();
                    } else {
                        status = resource.getHealth().getStatus();
                    }
                    sb.add(resource.getName() + " - " + status);
                }
            }
            return sb.toString();
        }
    }
}
