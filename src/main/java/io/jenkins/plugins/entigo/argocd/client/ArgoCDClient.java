package io.jenkins.plugins.entigo.argocd.client;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.entigo.argocd.model.Application;
import io.jenkins.plugins.entigo.argocd.model.ApplicationSyncRequest;
import io.jenkins.plugins.entigo.argocd.model.ApplicationWatchEvent;
import io.jenkins.plugins.entigo.argocd.model.UserInfo;
import org.glassfish.jersey.client.ChunkedInput;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public interface ArgoCDClient {

    Application syncApplication(String applicationName, ApplicationSyncRequest request);

    UserInfo getUserInfo();

    Application getApplication(String applicationName);

    // Annotation needed because of Spotbugs false positive https://github.com/spotbugs/spotbugs/pull/1248
    @NonNull
    ChunkedInput<ApplicationWatchEvent> watchApplication(String applicationName);

    void close();
}
