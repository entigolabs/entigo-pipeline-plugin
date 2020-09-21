package io.jenkins.plugins.entigo.pipeline;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
@Extension
public class PluginConfiguration extends GlobalConfiguration {

    public static PluginConfiguration get() {
        return ExtensionList.lookupSingleton(PluginConfiguration.class);
    }

    private ArgoCDConnectionsProperty argoCDConnectionsProperty;
    private List<ArgoCDConnection> argoCDConnections = new ArrayList<>();
    private transient final Map<String, ArgoCDConnection> namedArgoCDConnections = new HashMap<>();

    public PluginConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
        updateNamedArgoCDConnections();
    }

    public List<ArgoCDConnection> getArgoCDConnections() {
        return argoCDConnections;
    }

    @DataBoundSetter
    public void setArgoCDConnections(List<ArgoCDConnection> argoCDConnections) {
        this.argoCDConnections = argoCDConnections;
        updateNamedArgoCDConnections();
        save();
    }

    public ArgoCDConnectionsProperty getArgoCDConnectionsProperty() {
        return argoCDConnectionsProperty;
    }

    @DataBoundSetter
    public void setArgoCDConnectionsProperty(ArgoCDConnectionsProperty argoCDConnectionsProperty) {
        this.argoCDConnectionsProperty = argoCDConnectionsProperty;
        save();
    }

    public Map<String, ArgoCDConnection> getNamedArgoCDConnections() {
        return namedArgoCDConnections;
    }

    public ArgoCDConnection getArgoCDConnection(String connectionName) {
        return namedArgoCDConnections.get(connectionName);
    }

    private void updateNamedArgoCDConnections() {
        namedArgoCDConnections.clear();
        for (ArgoCDConnection argoCDConnection : argoCDConnections) {
            namedArgoCDConnections.put(argoCDConnection.getName(), argoCDConnection);
        }
    }
}
