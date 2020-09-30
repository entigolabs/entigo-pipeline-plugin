package io.jenkins.plugins.entigo.pipeline;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.util.CredentialsUtil;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

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

    private String gitCredentialsId;
    private ArgoCDConnectionsProperty argoCDConnectionsProperty;
    private List<ArgoCDConnection> argoCDConnections = new ArrayList<>();
    private final transient Map<String, ArgoCDConnection> namedArgoCDConnections = new HashMap<>();

    public PluginConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
        updateNamedArgoCDConnections();
    }

    public List<ArgoCDConnection> getArgoCDConnections() {
        return argoCDConnections;
    }

    public String getGitCredentialsId() {
        return gitCredentialsId;
    }

    @DataBoundSetter
    public void setGitCredentialsId(String gitCredentialsId) {
        this.gitCredentialsId = gitCredentialsId;
        save();
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

    public ListBoxModel doFillGitCredentialsIdItems(@AncestorInPath Item context,
                                                    @QueryParameter String credentialsId) {
        if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
                context != null && !context.hasPermission(Item.EXTENDED_READ)) {
            return new StandardListBoxModel().includeCurrentValue(credentialsId);
        }

        return new StandardListBoxModel()
                .includeEmptyValue()
                .includeMatchingAs(
                        context instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
                        context,
                        StandardUsernameCredentials.class,
                        Collections.emptyList(),
                        GitClient.CREDENTIALS_MATCHER)
                .includeCurrentValue(credentialsId);
    }

    public FormValidation doCheckGitCredentialsId(@AncestorInPath Item item,
                                                  @QueryParameter String value) {
        return CredentialsUtil.checkCredentialsId(item, value, StandardUsernameCredentials.class,
                Collections.emptyList());
    }
}
