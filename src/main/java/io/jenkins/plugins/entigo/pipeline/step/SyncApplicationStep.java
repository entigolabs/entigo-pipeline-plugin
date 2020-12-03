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
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-26
 */
public class SyncApplicationStep extends RequestStep {

    private final String name;
    private boolean wait = true;
    private boolean waitFailure = true;

    @DataBoundConstructor
    public SyncApplicationStep(@CheckForNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setWait(Boolean wait) {
        this.wait = wait;
    }

    public Boolean getWait() {
        return wait;
    }

    public boolean isWaitFailure() {
        return waitFailure;
    }

    @DataBoundSetter
    public void setWaitFailure(boolean waitFailure) {
        this.waitFailure = waitFailure;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new SyncApplicationStepExecution(stepContext, this);
    }

    @Extension
    public static class DescriptorImpl extends RequestStepDescriptor {

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
    }
}
