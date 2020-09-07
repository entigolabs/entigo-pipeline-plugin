package io.jenkins.plugins.entigo.argocd.client;

import hudson.ExtensionPoint;
import io.jenkins.plugins.entigo.rest.ClientException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-04
 */
public interface ArgoCDClientBuilder extends ExtensionPoint {

    ArgoCDClient buildClient(String uri, String token, boolean ignoreCertificateErrors) throws ClientException;
}
