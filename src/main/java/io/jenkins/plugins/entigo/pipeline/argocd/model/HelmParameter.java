package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class HelmParameter {

    private Boolean forceString;
    private String name;
    private String value;

    public Boolean getForceString() {
        return forceString;
    }

    public void setForceString(Boolean forceString) {
        this.forceString = forceString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
