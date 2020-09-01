package io.jenkins.plugins.entigo.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ApplicationSummary {
    
    private List<String> externalURLs;
    private List<String> images;

    public List<String> getExternalURLs() {
        return externalURLs;
    }

    public void setExternalURLs(List<String> externalURLs) {
        this.externalURLs = externalURLs;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
