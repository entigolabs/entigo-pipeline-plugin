package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-02
 */
public class DeleteApplicationProcess extends RequestProcess<Void> {

    private final ArgoCDClient argoCDClient;
    private final String applicationName;
    private final boolean cascade;

    public DeleteApplicationProcess(TaskListener listener, ArgoCDClient argoCDClient, String applicationName,
                                    boolean cascade) {
        super(listener);
        this.argoCDClient = argoCDClient;
        this.applicationName = applicationName;
        this.cascade = cascade;
    }

    protected ProcessResult<Void> run() {
        argoCDClient.deleteApplication(applicationName, cascade);
        return ProcessResult.success(null);
    }
}
