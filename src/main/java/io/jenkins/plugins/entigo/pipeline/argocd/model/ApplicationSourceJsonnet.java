package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourceJsonnet {

    private List<JsonnetVar> extVars;
    private List<JsonnetVar> tlas;

    public List<JsonnetVar> getExtVars() {
        return extVars;
    }

    public void setExtVars(List<JsonnetVar> extVars) {
        this.extVars = extVars;
    }

    public List<JsonnetVar> getTlas() {
        return tlas;
    }

    public void setTlas(List<JsonnetVar> tlas) {
        this.tlas = tlas;
    }
}
