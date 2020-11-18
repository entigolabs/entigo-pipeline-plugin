package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-18
 */
public enum OperationPhase {

    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    RUNNING("Running");

    private final String phase;

    OperationPhase(final String phase) {
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }
}
