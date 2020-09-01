package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class OperationInitiator {

    private Boolean automated;
    private String username;

    public Boolean getAutomated() {
        return automated;
    }

    public void setAutomated(Boolean automated) {
        this.automated = automated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
