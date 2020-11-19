package io.jenkins.plugins.entigo.pipeline.argocd.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgoCDConnectionMatcher that = (ArgoCDConnectionMatcher) o;
        return pattern.equals(that.pattern) &&
                connectionName.equals(that.connectionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ArgoCDConnectionMatcher> {

        public FormValidation doCheckPattern(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Pattern is required");
        }

        public FormValidation doCheckConnectionName(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Connection name is required");
            }
            ArgoCDConnection connection = PluginConfiguration.get().getArgoCDConnection(value);
            if (connection == null) {
                return FormValidation.error("Couldn't find a connection named " + value);
            } else {
                return FormValidation.ok();
            }
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
