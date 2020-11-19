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
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-19
 */
public class DeleteApplicationStep extends Step {

    private final String name;
    private boolean cascade = true;
    private String connectionSelector;

    @DataBoundConstructor
    public DeleteApplicationStep(@CheckForNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isCascade() {
        return cascade;
    }

    @DataBoundSetter
    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    @DataBoundSetter
    public void setConnectionSelector(String connectionSelector) {
        this.connectionSelector = connectionSelector;
    }

    public String getConnectionSelector() {
        return connectionSelector;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new DeleteApplicationStepExecution(stepContext, this);
    }

    public static class DeleteApplicationStepExecution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1;

        private final transient DeleteApplicationStep step;

        protected DeleteApplicationStepExecution(@Nonnull StepContext context, DeleteApplicationStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class), step.getConnectionSelector());
            ListenerUtil.println(listener, "Using ArgoCD connection: " + connection.getName());
            ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
            ListenerUtil.println(listener, String.format("Deleting ArgoCD application: %s, cascade: %s", step.getName(),
                    step.isCascade()));
            argoCDService.deleteApplication(step.getName(), step.isCascade());
            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Delete ArgoCD application";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, Run.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "deleteArgoApp";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Application name is required");
        }
    }
}
