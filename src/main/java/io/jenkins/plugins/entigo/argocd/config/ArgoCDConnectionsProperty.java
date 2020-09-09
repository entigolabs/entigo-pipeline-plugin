package io.jenkins.plugins.entigo.argocd.config;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.*;
import io.jenkins.plugins.entigo.PluginConfiguration;
import io.jenkins.plugins.entigo.argocd.client.ArgoCDClient;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
public class ArgoCDConnectionsProperty extends JobProperty<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(ArgoCDConnectionsProperty.class.getName());

    private final String selector;
    private final List<ArgoCDConnectionMatcher> matchers;

    @DataBoundConstructor
    public ArgoCDConnectionsProperty(String selector, List<ArgoCDConnectionMatcher> matchers) {
        this.selector = selector;
        this.matchers = matchers;
    }

    public String getSelector() {
        return selector;
    }

    public List<ArgoCDConnectionMatcher> getMatchers() {
        return matchers;
    }

    public ArgoCDClient getClient(String connectionName) throws AbortException {
        if (StringUtils.isNotEmpty(connectionName)) {
            ArgoCDConnection connection = PluginConfiguration.get().getArgoCDConnection(connectionName);
            if (connection != null) {
                return connection.getClient();
            }
        }

        return null;
    }

    public static  ArgoCDClient getClient(@NotNull Run<?, ?> build) throws AbortException {
        Job<?, ?> job = build.getParent();
        ArgoCDConnectionsProperty property = job.getProperty(ArgoCDConnectionsProperty.class);
        if (property == null) {
            throw new AbortException("ArgoCD connection was not set");
        } else {
            String connectionName = property.getConnectionName();
            ArgoCDClient client = property.getClient(connectionName);
            if (client == null) {
                throw new AbortException("Couldn't find an ArgoCD connection with name " + connectionName);
            } else {
                LOGGER.fine("Using ArgoCD connection " + connectionName);
                return client;
            }
        }
    }

    private String getConnectionName() throws AbortException {
        if (selector == null) {
            throw new AbortException("ArgoCD connection selector must not be null");
        }

        for (ArgoCDConnectionMatcher matcher : matchers) {
            if (selector.matches(matcher.getPattern())) {
                return matcher.getConnectionName();
            }
        }

        throw new AbortException("Couldn't find a matching ArgoCD connection with selector: " + selector);
    }

    @Extension
    @Symbol("argoCDConnections")
    public static class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "ArgoCD Connections";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

    }
}
