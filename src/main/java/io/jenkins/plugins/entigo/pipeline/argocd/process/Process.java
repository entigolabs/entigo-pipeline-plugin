package io.jenkins.plugins.entigo.pipeline.argocd.process;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-04
 */
public interface Process<T> {

    ProcessResult<T> start() throws Exception;

    void stop();
}
