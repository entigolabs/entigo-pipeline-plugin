package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;
import java.util.Map;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourceKustomize {

    private Map<String, String> commonLabels;
    private List<String> images;
    private String namePrefix;
    private String nameSuffix;
    private String version;

    public Map<String, String> getCommonLabels() {
        return commonLabels;
    }

    public void setCommonLabels(Map<String, String> commonLabels) {
        this.commonLabels = commonLabels;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
