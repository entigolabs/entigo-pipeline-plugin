package io.jenkins.plugins.entigo.pipeline.argocd.model;

public class SyncStrategyHook {

  private SyncStrategyApply syncStrategyApply;

  public SyncStrategyApply getSyncStrategyApply() {
    return syncStrategyApply;
  }

  public void setSyncStrategyApply(SyncStrategyApply syncStrategyApply) {
    this.syncStrategyApply = syncStrategyApply;
  }
}

