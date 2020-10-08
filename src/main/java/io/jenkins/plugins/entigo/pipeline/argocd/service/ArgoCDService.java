package io.jenkins.plugins.entigo.pipeline.argocd.service;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.rest.NotFoundException;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import org.glassfish.jersey.client.ChunkedInput;

import java.util.concurrent.*;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ArgoCDService {

    private final ArgoCDClient argoCDClient;

    public ArgoCDService(ArgoCDClient argoCDClient) {
        this.argoCDClient = argoCDClient;
    }

    public Application getApplication(String applicationName) throws AbortException {
        return getApplication(applicationName, null);
    }

    public Application getApplication(String applicationName, String projectName) throws AbortException {
        try {
            return argoCDClient.getApplication(applicationName, projectName);
        } catch (NotFoundException exception) {
            return null;
        } catch (ResponseException exception) {
            throw new AbortException("Failed to get ArgoCD application, error: " + exception.getMessage());
        }
    }

    public Application postApplication(Application application) throws AbortException {
        try {
            return argoCDClient.postApplication(application);
        } catch (ResponseException exception) {
            throw new AbortException("Failed to post the ArgoCD application, error: " + exception.getMessage());
        }
    }

    public void syncApplicationWithWait(TaskListener listener, String applicationName, Long timeout)
            throws AbortException {
        syncApplication(listener, applicationName);
        listener.getLogger().println("Waiting for application to sync, timeout is " + timeout + " seconds");
        waitApplicationStatus(applicationName, timeout);
        listener.getLogger().println("Application is synced and healthy");
    }

    public void syncApplication(TaskListener listener, String applicationName) throws AbortException {
        listener.getLogger().println("Syncing ArgoCD application...");
        SyncStrategy syncStrategy = new SyncStrategy();
        syncStrategy.setApply(new SyncStrategyApply(true));
        ApplicationSyncRequest syncRequest = new ApplicationSyncRequest();
        syncRequest.setName(applicationName);
        syncRequest.setPrune(true);
        syncRequest.setStrategy(syncStrategy);
        try {
            argoCDClient.syncApplication(applicationName, syncRequest);
        } catch (ResponseException exception) {
            throw new AbortException("Sync request to ArgoCD failed, error: " + exception.getMessage());
        }
    }

    public void waitApplicationStatus(String applicationName, Long timeout) throws AbortException {
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Void> task = executor.submit(() -> {
            watchApplicationEvents(applicationName);
            return null;
        });
        try {
            task.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new AbortException(e.getMessage());
        } catch (TimeoutException e) {
            throw new AbortException("Waiting for application sync timed out");
        } finally {
            task.cancel(true);
        }
    }

    private void watchApplicationEvents(String applicationName) throws AbortException {
        try (ChunkedInput<ApplicationWatchEvent> input = argoCDClient.watchApplication(applicationName)) {
            ApplicationWatchEvent event;
            while ((event = input.read()) != null) {
                if (isApplicationReady(event.getResult().getApplication())) {
                    return;
                }
            }
            // When event is null
            throw new AbortException("Failed to get an application event, either ArgoCD didn't send a response " +
                    "or response parsing failed");
        } catch (ResponseException exception) {
            throw new AbortException("Watching application events has failed, error: " + exception.getMessage());
        }
    }

    // Logic imported from the official ArgoCD CLI wait command src app.go method waitOnApplicationStatus
    private boolean isApplicationReady(Application application) {
        boolean operationInProgress = false;
        // consider the operation is in progress
        if (application.getOperation() != null) {
            // if it just got requested
            operationInProgress = true;
        } else if (application.getStatus().getOperationState() != null) {
            ApplicationStatus status = application.getStatus();
            if (status.getOperationState().getFinishedAt() == null || (status.getReconciledAt() == null ||
                    status.getReconciledAt().isBefore(status.getOperationState().getFinishedAt()))) {
                operationInProgress = true;
            }
        }

        if (operationInProgress) {
            return false;
        }

        return Health.HEALTHY.getStatus().equals(application.getStatus().getHealth().getStatus()) &&
                Sync.SYNCED.getStatus().equals(application.getStatus().getSync().getStatus());
    }
}
