package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
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
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Application name is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckWaitTimeout(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
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
