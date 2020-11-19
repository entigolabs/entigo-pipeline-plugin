package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourceKsonnet {

    private String environment;
    private List<KsonnetParameter> parameters;

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public List<KsonnetParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<KsonnetParameter> parameters) {
        this.parameters = parameters;
    }
}
