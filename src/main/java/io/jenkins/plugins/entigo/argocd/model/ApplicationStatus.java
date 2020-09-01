package io.jenkins.plugins.entigo.argocd.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ApplicationStatus {

    private List<ApplicationCondition> conditions;
    private HealthStatus health;
    private List<RevisionHistory> history;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime observedAt;
    private OperationState operationState;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime reconciledAt;
    private List<ResourceStatus> resources;
    private String sourceType;
    private ApplicationSummary summary;
    private SyncStatus sync;

    public List<ApplicationCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ApplicationCondition> conditions) {
        this.conditions = conditions;
    }

    public HealthStatus getHealth() {
        return health;
    }

    public void setHealth(HealthStatus health) {
        this.health = health;
    }

    public List<RevisionHistory> getHistory() {
        return history;
    }

    public void setHistory(List<RevisionHistory> history) {
        this.history = history;
    }

    public LocalDateTime getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(LocalDateTime observedAt) {
        this.observedAt = observedAt;
    }

    public OperationState getOperationState() {
        return operationState;
    }

    public void setOperationState(OperationState operationState) {
        this.operationState = operationState;
    }

    public LocalDateTime getReconciledAt() {
        return reconciledAt;
    }

    public void setReconciledAt(LocalDateTime reconciledAt) {
        this.reconciledAt = reconciledAt;
    }

    public List<ResourceStatus> getResources() {
        return resources;
    }

    public void setResources(List<ResourceStatus> resources) {
        this.resources = resources;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public ApplicationSummary getSummary() {
        return summary;
    }

    public void setSummary(ApplicationSummary summary) {
        this.summary = summary;
    }

    public SyncStatus getSync() {
        return sync;
    }

    public void setSync(SyncStatus sync) {
        this.sync = sync;
    }
}
