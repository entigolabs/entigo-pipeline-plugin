package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public enum Health {

    HEALTHY("Healthy");

    private final String status;

    Health(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
