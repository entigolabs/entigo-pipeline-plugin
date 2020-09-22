package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class SyncOperation {

    private Boolean dryRun;
    private List<String> manifests;
    private Boolean prune;
    private List<SyncResource> resources;
    private String revision;
    private ApplicationSource source;
    private List<String> syncOptions;
    private SyncStrategy syncStrategy;

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public List<String> getManifests() {
        return manifests;
    }

    public void setManifests(List<String> manifests) {
        this.manifests = manifests;
    }

    public Boolean getPrune() {
        return prune;
    }

    public void setPrune(Boolean prune) {
        this.prune = prune;
    }

    public List<SyncResource> getResources() {
        return resources;
    }

    public void setResources(List<SyncResource> resources) {
        this.resources = resources;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public void setSource(ApplicationSource source) {
        this.source = source;
    }

    public List<String> getSyncOptions() {
        return syncOptions;
    }

    public void setSyncOptions(List<String> syncOptions) {
        this.syncOptions = syncOptions;
    }

    public SyncStrategy getSyncStrategy() {
        return syncStrategy;
    }

    public void setSyncStrategy(SyncStrategy syncStrategy) {
        this.syncStrategy = syncStrategy;
    }
}
