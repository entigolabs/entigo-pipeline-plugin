package io.jenkins.plugins.entigo.argocd.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
// TODO Is time a timestamp or something else?
public class ManagedFieldsEntry {

    private String apiVersion;
    private String fieldsType;
    private Fields fieldsV1;
    private String manager;
    private String operation;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime time;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getFieldsType() {
        return fieldsType;
    }

    public void setFieldsType(String fieldsType) {
        this.fieldsType = fieldsType;
    }

    public Fields getFieldsV1() {
        return fieldsV1;
    }

    public void setFieldsV1(Fields fieldsV1) {
        this.fieldsV1 = fieldsV1;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
