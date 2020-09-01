package io.jenkins.plugins.entigo.step;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.PluginConfiguration;
import io.jenkins.plugins.entigo.argocd.service.ArgoCDService;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-26
 */
public class ApplicationSyncStep extends Builder implements SimpleBuildStep {

    private final String name;
    private Long waitTimeout;
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
    public void setWaitTimeout(Long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public Long getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public void perform(Run build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException {
        listener.getLogger().println("Syncing ArgoCD application...");
        ArgoCDService argoCDService = ExtensionList.lookupSingleton(ArgoCDService.class);
        argoCDService.syncApplication(name);
        Long timeout = waitTimeout == null ? PluginConfiguration.get().getArgoCDConfiguration().getAppWaitTimeout()
                : waitTimeout;
        if (!async) {
            listener.getLogger().println("Waiting for application to sync, timeout is " + timeout + " seconds");
            argoCDService.waitApplicationStatus(name, timeout);
            listener.getLogger().println("Application is synced and healthy");
        } else {
            listener.getLogger().println("Async mode enabled, skipping waiting for application to sync");
        }
    }

    @Symbol("syncArgoApp")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Sync ArgoCD applications";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }

        // Only works with web forms
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
