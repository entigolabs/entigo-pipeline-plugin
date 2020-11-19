package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSource {
    
    private String chart;
    private ApplicationSourceDirectory directory;
    private ApplicationSourceHelm helm;
    private ApplicationSourceKsonnet ksonnet;
    private ApplicationSourceKustomize kustomize;
    private String path;
    private ApplicationSourcePlugin plugin;
    private String repoURL;
    private String targetRevision;

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public ApplicationSourceDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(ApplicationSourceDirectory directory) {
        this.directory = directory;
    }

    public ApplicationSourceHelm getHelm() {
        return helm;
    }

    public void setHelm(ApplicationSourceHelm helm) {
        this.helm = helm;
    }

    public ApplicationSourceKsonnet getKsonnet() {
        return ksonnet;
    }

    public void setKsonnet(ApplicationSourceKsonnet ksonnet) {
        this.ksonnet = ksonnet;
    }

    public ApplicationSourceKustomize getKustomize() {
        return kustomize;
    }

    public void setKustomize(ApplicationSourceKustomize kustomize) {
        this.kustomize = kustomize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ApplicationSourcePlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(ApplicationSourcePlugin plugin) {
        this.plugin = plugin;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getTargetRevision() {
        return targetRevision;
    }

    public void setTargetRevision(String targetRevision) {
        this.targetRevision = targetRevision;
    }
}
