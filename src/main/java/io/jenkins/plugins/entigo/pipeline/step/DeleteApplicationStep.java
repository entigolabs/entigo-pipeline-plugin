package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
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
 * Date: 2020-11-19
 */
public class DeleteApplicationStep extends RequestStep {

    private final String name;
    private boolean cascade = true;

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

    @Override
    public StepExecution start(StepContext stepContext) {
        return new DeleteApplicationStepExecution(stepContext, this);
    }

    public static class DeleteApplicationStepExecution extends RequestStepExecution<Void> {

        private static final long serialVersionUID = 1;

        private final transient DeleteApplicationStep step;

        protected DeleteApplicationStepExecution(@Nonnull StepContext context, DeleteApplicationStep step) {
            super(context, step);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            getArgoCDService().deleteApplication(step.getName(), step.isCascade());
            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends RequestStepDescriptor {

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
