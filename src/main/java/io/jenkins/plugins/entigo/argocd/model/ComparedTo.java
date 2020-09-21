package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ComparedTo {
    
    private ApplicationDestination destination;
    private ApplicationSource source;

    public ApplicationDestination getDestination() {
        return destination;
    }

    public void setDestination(ApplicationDestination destination) {
        this.destination = destination;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public void setSource(ApplicationSource source) {
        this.source = source;
    }
}
