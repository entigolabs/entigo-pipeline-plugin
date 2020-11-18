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
public class ApplicationSyncStep extends Step {

    private final String name;
    private Integer waitTimeout;
    private boolean wait = true;
    private String connectionSelector;

    @DataBoundConstructor
    public ApplicationSyncStep(@CheckForNull String name) {
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

    @DataBoundSetter
    public void setWaitTimeout(Integer waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public Integer getWaitTimeout() {
        return waitTimeout;
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
        return new ApplicationSyncStepExecution(stepContext, this);
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
