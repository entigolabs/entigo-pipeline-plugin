package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
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
    private boolean async = false;

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
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class));
            ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
            if (step.async) {
                argoCDService.syncApplication(listener, step.name);
            } else {
                Long timeout = step.waitTimeout == null ? connection.getAppWaitTimeout() : Long.valueOf(step.waitTimeout);
                argoCDService.syncApplicationWithWait(listener, step.name, timeout);
            }
            return null;
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
            return FormValidationUtil.doCheckRequiredField(value, "Application name is required");
        }

        public FormValidation doCheckWaitTimeout(@QueryParameter String value) {
            return FormValidationUtil.doCheckTimeout(value, 1L, 1800L, false);
        }
    }
}
