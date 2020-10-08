package io.jenkins.plugins.entigo.pipeline.argocd.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-08
 */
public class ArgoCDConnectionMatcher extends AbstractDescribableImpl<ArgoCDConnectionMatcher> {

    private final String pattern;
    private final String connectionName;

    @DataBoundConstructor
    public ArgoCDConnectionMatcher(String pattern, String connectionName) {
        this.pattern = pattern;
        this.connectionName = connectionName;
    }

    public String getPattern() {
        return pattern;
    }

    public String getConnectionName() {
        return connectionName;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ArgoCDConnectionMatcher> {

        public FormValidation doCheckPattern(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Pattern is required");
        }

        public FormValidation doCheckConnectionName(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Connection name is required");
        }

        public ListBoxModel doFillConnectionNameItems() {
            ListBoxModel options = new ListBoxModel();
            List<ArgoCDConnection> connections = PluginConfiguration.get().getArgoCDConnections();
            for (ArgoCDConnection connection : connections) {
                options.add(connection.getName());
            }
            return options;
        }
    }
}
