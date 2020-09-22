package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class SyncPolicy {

    private SyncPolicyAutomated automated;
    private List<String> syncOptions;

    public SyncPolicyAutomated getAutomated() {
        return automated;
    }

    public void setAutomated(SyncPolicyAutomated automated) {
        this.automated = automated;
    }

    public List<String> getSyncOptions() {
        return syncOptions;
    }

    public void setSyncOptions(List<String> syncOptions) {
        this.syncOptions = syncOptions;
    }
}
