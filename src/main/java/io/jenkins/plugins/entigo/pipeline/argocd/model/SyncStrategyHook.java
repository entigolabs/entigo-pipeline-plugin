package io.jenkins.plugins.entigo.pipeline.argocd.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SyncStrategyHook {

  private final Boolean force;

  @JsonCreator
  public SyncStrategyHook(@JsonProperty("force") Boolean force) {
    this.force = force;
  }

  public Boolean getForce() {
    return force;
  }
}

