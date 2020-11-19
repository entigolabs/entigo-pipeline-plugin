package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class SyncPolicyAutomated {

    private Boolean prune;
    private Boolean selfHeal;

    public Boolean getPrune() {
        return prune;
    }

    public void setPrune(Boolean prune) {
        this.prune = prune;
    }

    public Boolean getSelfHeal() {
        return selfHeal;
    }

    public void setSelfHeal(Boolean selfHeal) {
        this.selfHeal = selfHeal;
    }
}
