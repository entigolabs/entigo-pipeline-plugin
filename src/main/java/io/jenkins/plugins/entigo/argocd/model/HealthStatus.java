package io.jenkins.plugins.entigo.argocd.model;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-01
 */
public class HealthStatus {

    private String message;
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
