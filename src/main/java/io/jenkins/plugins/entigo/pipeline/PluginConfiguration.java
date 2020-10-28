package io.jenkins.plugins.entigo.pipeline;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;

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
    private String defaultArgoCDConnection;
    private final transient Map<String, ArgoCDConnection> namedArgoCDConnections = new HashMap<>();

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

    public String getDefaultArgoCDConnection() {
        return defaultArgoCDConnection;
    }

    @DataBoundSetter
    public void setDefaultArgoCDConnection(String defaultArgoCDConnection) {
        this.defaultArgoCDConnection = defaultArgoCDConnection;
        save();
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

    public FormValidation doCheckDefaultArgoCDConnection(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.ok();
        }
        ArgoCDConnection connection = getArgoCDConnection(value);
        if (connection == null) {
            return FormValidation.error("Couldn't find a connection named " + value);
        } else {
            return FormValidation.ok();
        }
    }

    public ListBoxModel doFillDefaultArgoCDConnectionItems() {
        ListBoxModel connections = new ListBoxModel();
        connections.add("");
        for (ArgoCDConnection connection : getArgoCDConnections()) {
            if (connection.getName() != null) {
                connections.add(connection.getName());
            }
        }
        return connections;
    }

    // Required workaround as currently repeatableProperty can't save empty lists
    // Check https://reports.jenkins.io/core-taglib/jelly-taglib-ref.html#form:repeatableProperty
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        if (json.get("argoCDConnections") == null) {
            this.argoCDConnections = Collections.emptyList();
        }
        if (json.get("argoCDConnectionsProperty") == null) {
            this.argoCDConnectionsProperty = new ArgoCDConnectionsProperty(Collections.emptyList());
        }
        return super.configure(req, json);
    }
}
