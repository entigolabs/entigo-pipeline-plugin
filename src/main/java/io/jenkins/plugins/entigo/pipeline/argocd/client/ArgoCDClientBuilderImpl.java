package io.jenkins.plugins.entigo.pipeline.argocd.client;

import hudson.Extension;
import io.jenkins.plugins.entigo.pipeline.rest.ClientException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
@Extension
public class ArgoCDClientBuilderImpl implements ArgoCDClientBuilder {

    @Override
    public ArgoCDClient buildSecuredClient(String uri, String token) throws ClientException {
        return new ArgoCDClientImpl(uri, token, false);
    }

    @Override
    public ArgoCDClient buildUnsecuredClient(String uri, String token) throws ClientException {
        return new ArgoCDClientImpl(uri, token, true);
    }
}
