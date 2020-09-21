package io.jenkins.plugins.entigo.pipeline.argocd.model;

public class SyncStrategy {

  private SyncStrategyApply apply;
  private SyncStrategyHook hook;

  public SyncStrategyApply getApply() {
    return apply;
  }

  public void setApply(SyncStrategyApply apply) {
    this.apply = apply;
  }

  public SyncStrategyHook getHook() {
    return hook;
  }

  public void setHook(SyncStrategyHook hook) {
    this.hook = hook;
  }
}

