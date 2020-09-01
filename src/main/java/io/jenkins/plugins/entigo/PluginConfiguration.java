package io.jenkins.plugins.entigo;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.plugins.entigo.argocd.config.ArgoCDConfiguration;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
@Extension
public class PluginConfiguration extends GlobalConfiguration {

    public static PluginConfiguration get() {
        return ExtensionList.lookupSingleton(PluginConfiguration.class);
    }

    private ArgoCDConfiguration argoCDConfiguration;

    public PluginConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public ArgoCDConfiguration getArgoCDConfiguration() {
        return argoCDConfiguration;
    }

    @DataBoundSetter
    public void setArgoCDConfiguration(ArgoCDConfiguration argoCDConfiguration) {
        this.argoCDConfiguration = argoCDConfiguration;
        save();
    }
}
