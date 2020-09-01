package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ApplicationWatchEvent {

    private ApplicationWatchResult result;

    public ApplicationWatchResult getResult() {
        return result;
    }

    public void setResult(ApplicationWatchResult result) {
        this.result = result;
    }
}
