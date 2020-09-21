package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class Application {

    private MetaData metadata;
    private Operation operation;
    private ApplicationSpec spec;
    private ApplicationStatus status;

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public ApplicationSpec getSpec() {
        return spec;
    }

    public void setSpec(ApplicationSpec spec) {
        this.spec = spec;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
