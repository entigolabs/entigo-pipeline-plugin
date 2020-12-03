package io.jenkins.plugins.entigo.pipeline.rest;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-02
 */
public class ArgoCDException extends ResponseException{

    private final Integer code;

    public ArgoCDException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public ArgoCDException(String message, Integer code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
