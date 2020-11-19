package io.jenkins.plugins.entigo.pipeline.argocd.client;

import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSyncRequest;
import io.jenkins.plugins.entigo.pipeline.argocd.model.UserInfo;

import javax.ws.rs.core.Response;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public interface ArgoCDClient {

    Application syncApplication(String applicationName, ApplicationSyncRequest request);

    UserInfo getUserInfo();

    Application getApplication(String applicationName, String projectName);

    Response watchApplication(String applicationName, Integer readTimeout);

    void deleteApplication(String applicationName, boolean cascade);

    void close();
}
