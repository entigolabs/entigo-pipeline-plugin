package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ApplicationDestination {
    
    private String namespace;
    private String server;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
