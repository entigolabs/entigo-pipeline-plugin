package io.jenkins.plugins.entigo.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourceHelm {

    private List<HelmFileParameter> fileParameters;
    private List<HelmParameter> parameters;
    private String releaseName;
    private List<String> valueFiles;
    private String values;

    public List<HelmFileParameter> getFileParameters() {
        return fileParameters;
    }

    public void setFileParameters(List<HelmFileParameter> fileParameters) {
        this.fileParameters = fileParameters;
    }

    public List<HelmParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<HelmParameter> parameters) {
        this.parameters = parameters;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public List<String> getValueFiles() {
        return valueFiles;
    }

    public void setValueFiles(List<String> valueFiles) {
        this.valueFiles = valueFiles;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
