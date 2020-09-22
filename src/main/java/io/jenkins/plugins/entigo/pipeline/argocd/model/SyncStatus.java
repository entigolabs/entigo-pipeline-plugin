package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class SyncStatus {
    
    private ComparedTo comparedTo;
    private String revision;
    private String status;

    public ComparedTo getComparedTo() {
        return comparedTo;
    }

    public void setComparedTo(ComparedTo comparedTo) {
        this.comparedTo = comparedTo;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
