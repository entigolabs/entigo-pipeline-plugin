package io.jenkins.plugins.entigo.pipeline.rest;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-03
 */
public class RetryableException extends ResponseException {

    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
