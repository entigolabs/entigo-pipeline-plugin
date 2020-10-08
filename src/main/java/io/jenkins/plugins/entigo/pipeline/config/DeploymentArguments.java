package io.jenkins.plugins.entigo.pipeline.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Author: Märt Erlenheim
 * Date: 2020-10-05
 */
public class DeploymentArguments extends AbstractDescribableImpl<DeploymentArguments> {

    private final String repoUrl;
    private final String repoPath;
    private final String templatePath;
    private final String targetRevision;
    private final String clusterUrl;
    private final String targetNamespace;

    @DataBoundConstructor
    public DeploymentArguments(String repoUrl, String repoPath, String templatePath, String targetRevision,
                               String clusterUrl, String targetNamespace) {
        this.repoUrl = repoUrl;
        this.repoPath = repoPath;
        this.templatePath = templatePath;
        this.targetRevision = targetRevision;
        this.clusterUrl = clusterUrl;
        this.targetNamespace = targetNamespace;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getTargetRevision() {
        return targetRevision;
    }

    public String getClusterUrl() {
        return clusterUrl;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DeploymentArguments> {

        public FormValidation doCheckRepoUrl(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Repo url is required");
        }

        public FormValidation doCheckRepoPath(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Repo path is required");
        }

        public FormValidation doCheckTemplatePath(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Template path is required");
        }

        public FormValidation doCheckTargetRevision(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Target revision is required");
        }

        public FormValidation doCheckClusterUrl(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Cluster url is required");
        }

        public FormValidation doCheckTargetNamespace(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Target namespace is required");
        }
    }
}
