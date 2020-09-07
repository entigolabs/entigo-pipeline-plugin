package io.jenkins.plugins.entigo.argocd.client;

import hudson.Extension;
import io.jenkins.plugins.entigo.rest.ClientException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
@Extension
public class ArgoCDClientBuilderImpl implements ArgoCDClientBuilder {

    @Override
    public ArgoCDClient buildClient(String uri, String token, boolean ignoreCertificateErrors) throws ClientException {
        return new ArgoCDClientImpl(uri, token, ignoreCertificateErrors);
    }
}
