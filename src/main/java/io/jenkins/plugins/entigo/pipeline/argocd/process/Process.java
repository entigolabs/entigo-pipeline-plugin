package io.jenkins.plugins.entigo.pipeline.argocd.process;

import hudson.AbortException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-04
 */
public interface Process<T> {

    ProcessResult<T> start() throws AbortException;

    void stop();
}
