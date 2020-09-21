package io.jenkins.plugins.entigo.pipeline.argocd.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class ApplicationCondition {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastTransitionTime;
    private String message;
    private String type;

    public LocalDateTime getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(LocalDateTime lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
