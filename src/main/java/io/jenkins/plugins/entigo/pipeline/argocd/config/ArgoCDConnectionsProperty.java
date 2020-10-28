package io.jenkins.plugins.entigo.pipeline.argocd.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
public class ArgoCDConnectionsProperty extends JobProperty<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(ArgoCDConnectionsProperty.class.getName());
    private static final String SELECTOR_ENV_VAR = "ARGO_CD_SELECTOR";

    private final Set<ArgoCDConnectionMatcher> matchers;

    @DataBoundConstructor
    public ArgoCDConnectionsProperty(Set<ArgoCDConnectionMatcher> matchers) {
        this.matchers = matchers;
    }

    public Set<ArgoCDConnectionMatcher> getMatchers() {
        return matchers;
    }

    public static ArgoCDConnection getConnection(@NotNull Run<?, ?> build, EnvVars envVars, String connectionSelector)
            throws IOException {
        String connectionName = getConnectionName(build, envVars, connectionSelector);
        ArgoCDConnection connection = PluginConfiguration.get().getArgoCDConnection(connectionName);
        if (connection == null) {
            throw new AbortException(String.format("ArgoCD connection named \"%s\" not found, either use a correct " +
                    "selector or set a global default connection", connectionName));
        } else {
            return connection;
        }
    }

    public static ArgoCDConnectionsProperty getJobProperty(@NotNull Run<?, ?> build) throws IOException {
        Job<?, ?> job = build.getParent();
        ArgoCDConnectionsProperty property = job.getProperty(ArgoCDConnectionsProperty.class);
        if (property == null) {
            LOGGER.fine("No job specific ArgoCD connection property set, falling back to global config");
            property = PluginConfiguration.get().getArgoCDConnectionsProperty();
            if (property == null) {
                throw new AbortException("ArgoCD connection matchers were not set");
            }
        }
        return property;
    }

    private static String getSelector(EnvVars envVars, String connectionSelector) {
        if (StringUtils.isBlank(connectionSelector)) {
            String selector = envVars.get(SELECTOR_ENV_VAR);
            if (StringUtils.isBlank(selector)) {
                return null;
            } else {
                return selector;
            }
        } else {
            return connectionSelector;
        }
    }

    private static String getConnectionName(Run<?, ?> build, EnvVars envVars, String connectionSelector)
            throws IOException {
        String selector = getSelector(envVars, connectionSelector);
        if (selector == null) {
            return PluginConfiguration.get().getDefaultArgoCDConnection();
        } else {
            ArgoCDConnectionsProperty property = getJobProperty(build);
            for (ArgoCDConnectionMatcher matcher : property.getMatchers()) {
                if (selector.matches(matcher.getPattern())) {
                    return matcher.getConnectionName();
                }
            }

            throw new AbortException("Couldn't find a matching ArgoCD connection with selector: " + selector);
        }
    }

    @Extension
    @Symbol("argoCDConnections")
    public static class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return "ArgoCD Connections";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

    }
}
