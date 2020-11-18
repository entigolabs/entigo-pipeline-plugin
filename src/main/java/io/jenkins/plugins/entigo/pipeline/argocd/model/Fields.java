package io.jenkins.plugins.entigo.pipeline.argocd.model;

/**
 * Uses clones because of Spotbugs EI_EXPOSE_REP, see https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html
 * Author: Märt Erlenheim
 * Date: 2020-09-16
 */
public class Fields {

    private byte[] raw;

    public byte[] getRaw() {
        return raw.clone();
    }

    public void setRaw(byte[] raw) {
        this.raw = raw.clone();
    }
}
