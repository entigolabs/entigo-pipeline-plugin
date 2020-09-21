package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-26
 */
public class ApplicationSyncStep extends Step {

    private final String name;
    private Integer waitTimeout;
    private Boolean async = false;

    @DataBoundConstructor
    public ApplicationSyncStep(@CheckForNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Boolean getAsync() {
        return async;
    }

    @DataBoundSetter
    public void setWaitTimeout(Integer waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public Integer getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new ApplicationSyncStepExecution(stepContext, this);
    }

    public static class ApplicationSyncStepExecution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1; // Required by spotbugs

        private final transient ApplicationSyncStep step;

        protected ApplicationSyncStepExecution(@Nonnull StepContext context, ApplicationSyncStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            listener.getLogger().println("Syncing ArgoCD application...");
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class));
            ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
            argoCDService.syncApplication(step.name);
            if (step.async) {
                listener.getLogger().println("Async mode enabled, skip waiting for application to sync");
            } else {
                waitApplicationSync(listener, connection, argoCDService);
            }
            return null;
        }

        private void waitApplicationSync(TaskListener listener, ArgoCDConnection connection,
                                         ArgoCDService argoCDService) throws AbortException {
            Long timeout = step.waitTimeout == null ? connection.getAppWaitTimeout() : Long.valueOf(step.waitTimeout);
            listener.getLogger().println("Waiting for application to sync, timeout is " + timeout + " seconds");
            argoCDService.waitApplicationStatus(step.name, timeout);
            listener.getLogger().println("Application is synced and healthy");
        }

    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Sync ArgoCD application";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, Run.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "syncArgoApp";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Application name is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckWaitTimeout(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok();
            }
            try {
                long timeout = Long.parseLong(value);
                if (timeout < 1L || timeout > 1800L) {
                    return FormValidation.error("Timeout must be between 1 and 1800");
                }
            } catch (NumberFormatException exception) {
                return FormValidation.error("Must be a positive number");
            }
            return FormValidation.ok();
        }
    }
}
