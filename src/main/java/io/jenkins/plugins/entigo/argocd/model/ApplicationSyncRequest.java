package io.jenkins.plugins.entigo.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ApplicationSyncRequest {

  private Boolean dryRun;
  private List<Info> infos;
  private List<String> manifests;
  private String name;
  private Boolean prune;
  private List<SyncResource> resources;
  private String revision;
  private SyncStrategy strategy;

  public Boolean getDryRun() {
    return dryRun;
  }

  public void setDryRun(Boolean dryRun) {
    this.dryRun = dryRun;
  }

  public List<Info> getInfos() {
    return infos;
  }

  public void setInfos(List<Info> infos) {
    this.infos = infos;
  }

  public List<String> getManifests() {
    return manifests;
  }

  public void setManifests(List<String> manifests) {
    this.manifests = manifests;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public SyncStrategy getStrategy() {
    return strategy;
  }

  public void setStrategy(SyncStrategy strategy) {
    this.strategy = strategy;
  }
}

