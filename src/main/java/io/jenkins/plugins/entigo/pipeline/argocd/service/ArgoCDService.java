package io.jenkins.plugins.entigo.pipeline.argocd.service;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.argocd.process.ArgoCDWaitProcess;
import io.jenkins.plugins.entigo.pipeline.rest.NotFoundException;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import io.jenkins.plugins.entigo.pipeline.argocd.process.TimeoutExecution;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.jenkinsci.plugins.workflow.steps.StepContext;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ArgoCDService {

    private final ArgoCDClient argoCDClient;

    public ArgoCDService(ArgoCDClient argoCDClient) {
        this.argoCDClient = argoCDClient;
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

    public void syncApplication(TaskListener listener, String applicationName) throws AbortException {
        ListenerUtil.println(listener, "Syncing ArgoCD application...");
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

    public TimeoutExecution waitApplicationStatus(String applicationName, Long timeout, StepContext context,
                                                  TaskListener listener) {
        ListenerUtil.println(listener, "Waiting for application to sync, timeout is " + timeout + " seconds");
        ArgoCDWaitProcess process = new ArgoCDWaitProcess(context, listener, argoCDClient, applicationName);
        TimeoutExecution execution = new TimeoutExecution(process, timeout, listener);
        execution.start();
        return execution;
    }

}
