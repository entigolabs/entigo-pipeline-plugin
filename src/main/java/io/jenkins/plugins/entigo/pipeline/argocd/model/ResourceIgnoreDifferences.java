package io.jenkins.plugins.entigo.pipeline.argocd.model;

import java.util.List;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class ResourceIgnoreDifferences {
    
    private String group;
    private List<String> jsonPointers;
    private String kind;
    private String name;
    private String namespace;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getJsonPointers() {
        return jsonPointers;
    }

    public void setJsonPointers(List<String> jsonPointers) {
        this.jsonPointers = jsonPointers;
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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
