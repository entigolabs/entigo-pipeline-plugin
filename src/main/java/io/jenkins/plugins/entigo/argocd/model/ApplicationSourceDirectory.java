package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourceDirectory {

    private ApplicationSourceJsonnet jsonnet;
    private Boolean recurse;

    public ApplicationSourceJsonnet getJsonnet() {
        return jsonnet;
    }

    public void setJsonnet(ApplicationSourceJsonnet jsonnet) {
        this.jsonnet = jsonnet;
    }

    public Boolean getRecurse() {
        return recurse;
    }

    public void setRecurse(Boolean recurse) {
        this.recurse = recurse;
    }
}
