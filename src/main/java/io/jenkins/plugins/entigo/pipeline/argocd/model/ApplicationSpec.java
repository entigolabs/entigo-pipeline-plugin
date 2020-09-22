package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSpec {

    private ApplicationDestination destination;
    private List<ResourceIgnoreDifferences> ignoreDifferences;
    private List<Info> info;
    private String project;
    private String revisionHistoryLimit;
    private ApplicationSource source;
    private SyncPolicy syncPolicy;

    public ApplicationDestination getDestination() {
        return destination;
    }

    public void setDestination(ApplicationDestination destination) {
        this.destination = destination;
    }

    public List<ResourceIgnoreDifferences> getIgnoreDifferences() {
        return ignoreDifferences;
    }

    public void setIgnoreDifferences(List<ResourceIgnoreDifferences> ignoreDifferences) {
        this.ignoreDifferences = ignoreDifferences;
    }

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getRevisionHistoryLimit() {
        return revisionHistoryLimit;
    }

    public void setRevisionHistoryLimit(String revisionHistoryLimit) {
        this.revisionHistoryLimit = revisionHistoryLimit;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public void setSource(ApplicationSource source) {
        this.source = source;
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public void setSyncPolicy(SyncPolicy syncPolicy) {
        this.syncPolicy = syncPolicy;
    }
}
