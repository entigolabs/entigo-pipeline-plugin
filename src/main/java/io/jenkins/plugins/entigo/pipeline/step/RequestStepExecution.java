package io.jenkins.plugins.entigo.pipeline.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-03
 */
public abstract class RequestStepExecution<T> extends SynchronousNonBlockingStepExecution<T> {

    private static final long serialVersionUID = 1;

    private final transient RequestStep step;
    private transient ArgoCDConnection argoCDConnection = null;
    private transient ArgoCDService argoCDService = null;

    protected RequestStepExecution(@Nonnull StepContext context, RequestStep step) {
        super(context);
        this.step = step;
    }

    protected ArgoCDConnection getArgoCDConnection() throws IOException, InterruptedException {
        if (this.argoCDConnection == null) {
            this.argoCDConnection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class), step.getConnectionSelector());
        }
        return this.argoCDConnection;
    }

    protected ArgoCDService getArgoCDService() throws IOException, InterruptedException {
        if (this.argoCDService == null) {
            ArgoCDConnection connection = getArgoCDConnection();
            TaskListener listener = getContext().get(TaskListener.class);
            ListenerUtil.println(listener, "Using ArgoCD connection: " + connection.getName());
            Long timeout = step.getWaitTimeout() == null ? connection.getAppWaitTimeout() : Long.valueOf(step.getWaitTimeout());
            this.argoCDService = new ArgoCDService(connection.getClient(), listener, timeout);
        }
        return argoCDService;
    }

    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {
        if (this.argoCDService != null) {
            this.argoCDService.stop();
        }
        super.stop(cause);
    }
}
