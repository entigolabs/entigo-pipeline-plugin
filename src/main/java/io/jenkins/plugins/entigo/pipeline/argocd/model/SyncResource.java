package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class SyncResource {

    private String group;
    private String kind;
    private String name;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
