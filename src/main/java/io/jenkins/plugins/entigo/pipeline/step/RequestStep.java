package io.jenkins.plugins.entigo.pipeline.step;

import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-03
 */
public abstract class RequestStep extends Step {

    private Integer waitTimeout;
    private String connectionSelector;

    public Integer getWaitTimeout() {
        return waitTimeout;
    }

    @DataBoundSetter
    public void setWaitTimeout(Integer waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public String getConnectionSelector() {
        return connectionSelector;
    }

    @DataBoundSetter
    public void setConnectionSelector(String connectionSelector) {
        this.connectionSelector = connectionSelector;
    }

    public abstract static class RequestStepDescriptor extends StepDescriptor {

        public FormValidation doCheckWaitTimeout(@QueryParameter String value) {
            return FormValidationUtil.doCheckTimeout(value, 1L, 1800L, false);
        }
    }
}
