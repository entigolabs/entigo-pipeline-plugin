package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.rest.NotFoundException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-02
 */
public class GetApplicationProcess extends RequestProcess<Application> {

    private final ArgoCDClient argoCDClient;
    private final String applicationName;
    private final String projectName;

    public GetApplicationProcess(TaskListener listener, ArgoCDClient argoCDClient, String applicationName,
                                 String projectName) {
        super(listener);
        this.argoCDClient = argoCDClient;
        this.applicationName = applicationName;
        this.projectName = projectName;
    }

    protected ProcessResult<Application> run() {
        try {
            Application application = argoCDClient.getApplication(applicationName, projectName);
            return ProcessResult.success(application);
        } catch (NotFoundException exception) {
            return ProcessResult.success(null);
        }
    }
}
