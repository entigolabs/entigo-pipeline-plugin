package io.jenkins.plugins.entigo.pipeline.argocd.process;

import org.jenkinsci.plugins.workflow.steps.StepContext;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-06
 */
public abstract class AbstractProcess implements Process {

    private final StepContext context;

    protected AbstractProcess(StepContext context) {
        this.context = context;
    }

    protected void success(Object result) {
        context.onSuccess(result);
    }

    public void failure(Throwable cause) {
        context.onFailure(cause);
    }
}
