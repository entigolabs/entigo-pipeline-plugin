package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
// TODO Not all fields have been mapped
public class Application {

    private Operation operation;
    private ApplicationStatus status;

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
