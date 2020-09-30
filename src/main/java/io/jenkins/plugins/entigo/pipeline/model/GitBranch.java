package io.jenkins.plugins.entigo.pipeline.model;

import java.util.Objects;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-30
 */
public class GitBranch {

    private String url;
    private String name;

    public GitBranch(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitBranch gitBranch = (GitBranch) o;
        return url.equals(gitBranch.url) &&
                name.equals(gitBranch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }

    @Override
    public String toString() {
        return "GitBranch{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
