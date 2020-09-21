package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class Operation {

    private List<Info> info;
    private OperationInitiator initiatedBy;
    private SyncOperation sync;

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    public OperationInitiator getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(OperationInitiator initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public SyncOperation getSync() {
        return sync;
    }

    public void setSync(SyncOperation sync) {
        this.sync = sync;
    }
}
