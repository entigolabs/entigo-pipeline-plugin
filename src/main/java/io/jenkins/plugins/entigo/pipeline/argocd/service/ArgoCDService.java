package io.jenkins.plugins.entigo.pipeline.argocd.service;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.glassfish.jersey.client.ChunkedInput;

import java.util.concurrent.*;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ArgoCDService {

    private static final int INITIAL_RETRY_DELAY = 1;
    private static final int MAX_RETRY_DELAY = 30;

    private final ArgoCDClient argoCDClient;
    private TaskListener listener = null;

    public ArgoCDService(ArgoCDClient argoCDClient) {
        this.argoCDClient = argoCDClient;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public void syncApplication(String applicationName) throws AbortException {
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
            throw new AbortException("Step was interrupted");
        } catch (ExecutionException e) {
            throw new AbortException(e.getMessage());
        } catch (TimeoutException e) {
            throw new AbortException("Waiting for application sync timed out");
        } finally {
            task.cancel(true);
        }
    }

    private void watchApplicationEvents(String applicationName) throws InterruptedException {
        long retryDelay = INITIAL_RETRY_DELAY;
        while (true) {
            try (ChunkedInput<ApplicationWatchEvent> input = argoCDClient.watchApplication(applicationName)) {
                ApplicationWatchEvent event;
                while ((event = input.read()) != null) {
                    retryDelay = INITIAL_RETRY_DELAY;
                    if (isApplicationReady(event.getResult().getApplication())) {
                        return;
                    }
                }
                // When event is null
                ListenerUtil.println(listener, String.format("Connection was interrupted, retrying in %d seconds",
                        retryDelay));
            } catch (ResponseException exception) {
                ListenerUtil.println(listener, String.format("Request failed, retrying in %d seconds," +
                        " exception message: %s", retryDelay, exception.getMessage()));
            }
            Thread.sleep(retryDelay * 1000L);
            if (retryDelay < MAX_RETRY_DELAY) {
                retryDelay = Math.min(MAX_RETRY_DELAY, retryDelay * 2);
            }
        }
    }

    // Logic imported from the official ArgoCD CLI wait command src app.go method waitOnApplicationStatus
    private boolean isApplicationReady(Application application) {
        if (application.getOperation() != null) {
            return false;
        } else if (application.getStatus().getOperationState() != null) {
            ApplicationStatus status = application.getStatus();
            if (status.getOperationState().getFinishedAt() == null || (status.getReconciledAt() == null ||
                    status.getReconciledAt().isBefore(status.getOperationState().getFinishedAt()))) {
                ListenerUtil.println(listener, "Operation is in progress, phase: " +
                        application.getStatus().getOperationState().getPhase());
                return false;
            }
        }

        String healthStatus = application.getStatus().getHealth().getStatus();
        String syncStatus = application.getStatus().getSync().getStatus();
        ListenerUtil.println(listener, String.format("Operation finished, sync status: %s, health status: %s",
                syncStatus, healthStatus));
        return Health.HEALTHY.getStatus().equals(healthStatus) && Sync.SYNCED.getStatus().equals(syncStatus);
    }
}
