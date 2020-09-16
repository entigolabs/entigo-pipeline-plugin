package io.jenkins.plugins.entigo.argocd.config;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import io.jenkins.plugins.entigo.PluginConfiguration;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
public class ArgoCDConnectionsProperty extends JobProperty<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(ArgoCDConnectionsProperty.class.getName());
    private static final String SELECTOR_ENV_VAR = "ARGO_CD_SELECTOR";

    private final List<ArgoCDConnectionMatcher> matchers;

    @DataBoundConstructor
    public ArgoCDConnectionsProperty(List<ArgoCDConnectionMatcher> matchers) {
        this.matchers = matchers;
    }

    public List<ArgoCDConnectionMatcher> getMatchers() {
        return matchers;
    }

    public static ArgoCDConnectionsProperty getJobProperty(@NotNull Run<?, ?> build) throws IOException {
        Job<?, ?> job = build.getParent();
        ArgoCDConnectionsProperty property = job.getProperty(ArgoCDConnectionsProperty.class);
        if (property == null) {
            LOGGER.fine("No job specific ArgoCD connection property set, falling back to global config");
            property = PluginConfiguration.get().getArgoCDConnectionsMatcher();
            if (property == null) {
                throw new AbortException("ArgoCD connection matcher was not set");
            }
        }
        return property;
    }

    private String getConnectionName(EnvVars envVars) throws AbortException {
        String selector = envVars.get(SELECTOR_ENV_VAR);
        if (StringUtils.isEmpty(selector)) {
            throw new AbortException("ArgoCD selector env variable not set, variable name must be " + SELECTOR_ENV_VAR);
        }

        for (ArgoCDConnectionMatcher matcher : matchers) {
            if (selector.matches(matcher.getPattern())) {
                return matcher.getConnectionName();
            }
        }

        throw new AbortException("Couldn't find a matching ArgoCD connection with selector: " + selector);
    }

    public static ArgoCDConnection getConnection(@NotNull Run<?, ?> build, EnvVars envVars) throws IOException {
        ArgoCDConnectionsProperty property = getJobProperty(build);
        String connectionName = property.getConnectionName(envVars);
        ArgoCDConnection connection = PluginConfiguration.get().getArgoCDConnection(connectionName);
        if (connection == null) {
            throw new AbortException("Couldn't find an ArgoCD connection with name " + connectionName);
        } else {
            return connection;
        }
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
