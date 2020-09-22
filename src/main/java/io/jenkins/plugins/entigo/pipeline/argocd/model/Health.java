package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public enum Health {

    HEALTHY("Healthy"),
    PROGRESSING("Progressing"),
    SUSPENDED("Suspended"),
    DEGRADED("Degraded"),
    MISSING("Missing"),
    UNKNOWN("Unknown");

    private final String status;

    Health(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
