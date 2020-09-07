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

import java.util.logging.Logger;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
// TODO Add selector filter for finding a suitable client
public class ArgoCDConnectionProperty extends JobProperty<Job<?, ?>> {

    private static final Logger log = Logger.getLogger(ArgoCDConnectionProperty.class.getName());

    private final String argoCDConnection;

    @DataBoundConstructor
    public ArgoCDConnectionProperty(String argoCDConnection) {
        this.argoCDConnection = argoCDConnection;
    }

    public String getArgoCDConnection() {
        return argoCDConnection;
    }

    public ArgoCDClient getClient() throws AbortException {
        if (StringUtils.isNotEmpty(argoCDConnection)) {
            ArgoCDConnection connection = PluginConfiguration.get().getArgoCDConnection(argoCDConnection);
            if (connection != null) {
                return connection.getClient();
            }
        }

        return null;
    }

    public static  ArgoCDClient getClient(@NotNull Run<?, ?> build) throws AbortException {
        Job<?, ?> job = build.getParent();
        ArgoCDConnectionProperty property = job.getProperty(ArgoCDConnectionProperty.class);
        if (property == null) {
            throw new AbortException("ArgoCD connection was not set");
        } else {
            ArgoCDClient client = property.getClient();
            if (client == null) {
                throw new AbortException("Couldn't find an ArgoCD connection with name "
                        + property.getArgoCDConnection());
            } else {
                log.fine("Using ArgoCD connection " + property.getArgoCDConnection());
                return client;
            }
        }
    }

    @Extension
    @Symbol("argoCDConnection")
    public static class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "ArgoCD Connection";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }
    }
}
