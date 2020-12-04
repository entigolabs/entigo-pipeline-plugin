package io.jenkins.plugins.entigo.pipeline.rest;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-27
 */
public class ResponseException extends RuntimeException {

    private static final long serialVersionUID = 8621672386353776785L;

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
