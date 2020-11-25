package io.jenkins.plugins.entigo.pipeline.step;

import hudson.EnvVars;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.process.TimeoutExecution;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import javax.annotation.Nonnull;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-09
 */
public class SyncApplicationStepExecution extends AbstractStepExecutionImpl {

    private static final long serialVersionUID = 1; // Required by spotbugs

    private final transient SyncApplicationStep step;
    private transient Thread executing;
    private transient TimeoutExecution waitExecution = null;

    protected SyncApplicationStepExecution(@Nonnull StepContext context, SyncApplicationStep step) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() throws Exception {
        this.executing = Thread.currentThread();
        TaskListener listener = getContext().get(TaskListener.class);
        ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                getContext().get(EnvVars.class), step.getConnectionSelector());
        ListenerUtil.println(listener, "Using ArgoCD connection: " + connection.getName());
        ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
        argoCDService.syncApplication(listener, step.getName());
        if (Boolean.TRUE.equals(step.getWait())) {
            waitApplicationSync(listener, connection, argoCDService);
            this.executing = null;
            return false;
        } else {
            ListenerUtil.println(listener, "Waiting disabled, won't wait for sync to complete");
            getContext().onSuccess(null);
            this.executing = null;
            return true;
        }
    }

    private synchronized void waitApplicationSync(TaskListener listener, ArgoCDConnection connection,
                                                  ArgoCDService argoCDService) {
        Long timeout = step.getWaitTimeout() == null ? connection.getAppWaitTimeout() : Long.valueOf(step.getWaitTimeout());
        this.waitExecution = argoCDService.waitApplicationStatus(step.getName(), timeout, step.isWaitFailure(),
                getContext(), listener);
    }

    @Override
    public synchronized void stop(@Nonnull Throwable cause) throws Exception {
        TimeoutExecution timeoutExecution = this.waitExecution;
        if (timeoutExecution != null) {
            timeoutExecution.stop();
        }
        Thread executionThread = this.executing;
        if (executionThread != null) {
            if (executionThread instanceof Executor) {
                ((Executor)executionThread).interrupt(Result.ABORTED);
            } else {
                executionThread.interrupt();
            }
        }
        super.stop(cause);
    }
}
