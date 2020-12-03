package io.jenkins.plugins.entigo.pipeline.argocd.process;

import java.io.IOException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-12-03
 */
public class ProcessException extends IOException {

    private static final long serialVersionUID = -5253921659719467221L;

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
