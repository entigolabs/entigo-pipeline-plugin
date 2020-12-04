package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-02
 */
public class SyncApplicationProcess extends RequestProcess<Application> {

    private final ArgoCDClient argoCDClient;
    private final String applicationName;
    private final ApplicationSyncRequest syncRequest;

    public SyncApplicationProcess(TaskListener listener, ArgoCDClient argoCDClient, String applicationName,
                                  ApplicationSyncRequest syncRequest) {
        super(listener);
        this.argoCDClient = argoCDClient;
        this.applicationName = applicationName;
        this.syncRequest = syncRequest;
    }

    protected ProcessResult<Application> run() {
        Application application = argoCDClient.syncApplication(applicationName, syncRequest);
        return ProcessResult.success(application);
    }
}
