package io.jenkins.plugins.entigo.pipeline.argocd.client;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSyncRequest;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationWatchEvent;
import io.jenkins.plugins.entigo.pipeline.argocd.model.UserInfo;
import org.glassfish.jersey.client.ChunkedInput;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public interface ArgoCDClient {

    Application syncApplication(String applicationName, ApplicationSyncRequest request);

    UserInfo getUserInfo();

    Application getApplication(String applicationName, String projectName);

    // Annotation needed because of Spotbugs false positive https://github.com/spotbugs/spotbugs/pull/1248
    @NonNull
    ChunkedInput<ApplicationWatchEvent> watchApplication(String applicationName);

    Application postApplication(Application application);

    void close();
}
