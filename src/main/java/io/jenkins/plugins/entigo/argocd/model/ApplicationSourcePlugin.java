package io.jenkins.plugins.entigo.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ApplicationSourcePlugin {

    private List<EnvEntry> env;
    private String name;

    public List<EnvEntry> getEnv() {
        return env;
    }

    public void setEnv(List<EnvEntry> env) {
        this.env = env;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
