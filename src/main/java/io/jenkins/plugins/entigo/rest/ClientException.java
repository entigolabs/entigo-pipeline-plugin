package io.jenkins.plugins.entigo.rest;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-07
 */
public class ClientException extends RuntimeException {

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
