package io.jenkins.plugins.entigo.pipeline.step;

import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import javax.annotation.Nonnull;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-09
 */
public class SyncApplicationStepExecution extends RequestStepExecution<Void> {

    private static final long serialVersionUID = 1; // Required by spotbugs

    private final transient SyncApplicationStep step;

    protected SyncApplicationStepExecution(@Nonnull StepContext context, SyncApplicationStep step) {
        super(context, step);
        this.step = step;
    }

    @Override
    public Void run() throws Exception {
        ArgoCDService argoCDService = getArgoCDService();
        argoCDService.syncApplication(step.getName());
        if (Boolean.TRUE.equals(step.getWait())) {
            argoCDService.waitApplicationStatus(step.getName(), step.isWaitFailure());
        } else {
            TaskListener listener = getContext().get(TaskListener.class);
            ListenerUtil.println(listener, "Waiting disabled, won't wait for sync to complete");
        }
        return null;
    }
}
