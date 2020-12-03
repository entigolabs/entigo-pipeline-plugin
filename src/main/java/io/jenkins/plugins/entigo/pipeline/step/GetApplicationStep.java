package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSource;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-18
 */
public class GetApplicationStep extends RequestStep {

    private final String name;
    private String projectName;

    @DataBoundConstructor
    public GetApplicationStep(@CheckForNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setProjectName(String projectName) {
        this.projectName = StringUtils.stripToNull(projectName);
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new GetApplicationStepExecution(stepContext, this);
    }

    public static class GetApplicationStepExecution extends RequestStepExecution<Map<String, String>> {

        private static final long serialVersionUID = 1;

        private final transient GetApplicationStep step;

        protected GetApplicationStepExecution(@Nonnull StepContext context, GetApplicationStep step) {
            super(context, step);
            this.step = step;
        }

        @Override
        protected Map<String, String> run() throws Exception {
            Application application = getArgoCDService().getApplication(step.getName(), step.getProjectName());
            return getApplicationInfo(application, getConnection());
        }

        private Map<String, String> getApplicationInfo(Application application, ArgoCDConnection connection) {
            if (application == null) {
                return null;
            } else {
                Map<String, String> appInfo = new HashMap<>();
                ApplicationSource source = application.getSpec().getSource();
                appInfo.put("repoUrl", source.getRepoURL());
                appInfo.put("revision", source.getTargetRevision());
                appInfo.put("path", source.getPath());
                appInfo.put("connectionName", connection.getName());
                return appInfo;
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends RequestStepDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Get ArgoCD application";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, Run.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "getArgoApp";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Application name is required");
        }
    }
}
