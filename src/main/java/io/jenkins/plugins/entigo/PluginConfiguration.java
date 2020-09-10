package io.jenkins.plugins.entigo;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.plugins.entigo.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.argocd.config.ArgoCDConnectionsProperty;
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

    private ArgoCDConnectionsProperty argoCDConnectionsMatcher;
    private List<ArgoCDConnection> argoCDConnections = new ArrayList<>();
    private transient final Map<String, ArgoCDConnection> argoCDConnectionsMap = new HashMap<>();

    public PluginConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
        updateArgoCDConnectionsMap();
    }

    public List<ArgoCDConnection> getArgoCDConnections() {
        return argoCDConnections;
    }

    @DataBoundSetter
    public void setArgoCDConnections(List<ArgoCDConnection> argoCDConnections) {
        this.argoCDConnections = argoCDConnections;
        updateArgoCDConnectionsMap();
        save();
    }

    public ArgoCDConnectionsProperty getArgoCDConnectionsMatcher() {
        return argoCDConnectionsMatcher;
    }

    @DataBoundSetter
    public void setArgoCDConnectionsMatcher(ArgoCDConnectionsProperty argoCDConnectionsMatcher) {
        this.argoCDConnectionsMatcher = argoCDConnectionsMatcher;
        save();
    }

    public Map<String, ArgoCDConnection> getArgoCDConnectionsMap() {
        return argoCDConnectionsMap;
    }

    public ArgoCDConnection getArgoCDConnection(String connectionName) {
        return argoCDConnectionsMap.get(connectionName);
    }

    private void updateArgoCDConnectionsMap() {
        argoCDConnectionsMap.clear();
        for (ArgoCDConnection argoCDConnection : argoCDConnections) {
            argoCDConnectionsMap.put(argoCDConnection.getName(), argoCDConnection);
        }
    }
}
