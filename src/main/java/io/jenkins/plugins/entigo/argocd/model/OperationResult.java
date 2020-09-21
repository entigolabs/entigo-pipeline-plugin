package io.jenkins.plugins.entigo.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class OperationResult {

    private List<ResourceResult> resources;
    private String revision;
    private ApplicationSource source;

    public List<ResourceResult> getResources() {
        return resources;
    }

    public void setResources(List<ResourceResult> resources) {
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
}
