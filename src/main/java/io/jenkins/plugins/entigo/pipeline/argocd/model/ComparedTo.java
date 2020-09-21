package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
// TODO Map ApplicationSource source from OpenApi
public class ComparedTo {
    
    private ApplicationDestination destination;

    public ApplicationDestination getDestination() {
        return destination;
    }

    public void setDestination(ApplicationDestination destination) {
        this.destination = destination;
    }
}
