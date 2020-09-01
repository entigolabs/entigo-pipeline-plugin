package io.jenkins.plugins.entigo.argocd.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SyncStrategyApply {

  private final Boolean force;

  @JsonCreator
  public SyncStrategyApply(@JsonProperty("force") Boolean force) {
    this.force = force;
  }

  public Boolean getForce() {
    return force;
  }
}

