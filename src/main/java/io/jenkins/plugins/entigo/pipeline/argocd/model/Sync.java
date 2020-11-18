package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public enum Sync {

    SYNCED("Synced"),
    OUT_OF_SYNC("OutOfSync"),
    SYNC_FAILED("SyncFailed"),
    UNKNOWN("Unknown");

    private final String status;

    Sync(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
