package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
// TODO Map V1alpha1ApplicationSource source from OpenApi
public class OperationResult {

    private List<ResourceResult> resources;
    private String revision;

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
}
