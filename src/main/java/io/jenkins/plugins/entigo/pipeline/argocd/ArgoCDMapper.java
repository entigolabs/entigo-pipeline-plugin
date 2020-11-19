package io.jenkins.plugins.entigo.pipeline.argocd;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;

/**
 * Author: Märt Erlenheim
 * Date: 2020-10-08
 */
@Extension
public class ArgoCDMapper {

    public static ArgoCDMapper get() {
        return ExtensionList.lookupSingleton(ArgoCDMapper.class);
    }

    public Application createApplication(String applicationName, String projectName, String repoUrl, String path,
                                         String targetRevision, String clusterUrl, String clusterNamespace) {
        Application application = new Application();
        application.setMetadata(createApplicationMetaData(applicationName));
        application.setSpec(createApplicationSpec(projectName, repoUrl, path, targetRevision, clusterUrl,
                clusterNamespace));
        return application;
    }

    private MetaData createApplicationMetaData(String applicationName) {
        MetaData metaData = new MetaData();
        metaData.setName(applicationName);
        return metaData;
    }

    private ApplicationSpec createApplicationSpec(String projectName, String repoUrl, String path,
                                                  String targetRevision, String clusterUrl, String clusterNamespace) {
        ApplicationSpec spec = new ApplicationSpec();
        spec.setProject(projectName);

        ApplicationSource source = new ApplicationSource();
        source.setRepoURL(repoUrl);
        source.setTargetRevision(targetRevision);
        source.setPath(path);
        spec.setSource(source);

        ApplicationDestination destination = new ApplicationDestination();
        destination.setServer(clusterUrl);
        destination.setNamespace(clusterNamespace);
        spec.setDestination(destination);

        return spec;
    }
}
