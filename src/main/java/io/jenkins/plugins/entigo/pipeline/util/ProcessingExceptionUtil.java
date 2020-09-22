package io.jenkins.plugins.entigo.pipeline.util;

import javax.ws.rs.ProcessingException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public class ProcessingExceptionUtil {

    private ProcessingExceptionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getExceptionMessage(ProcessingException exception) {
        Throwable cause = exception.getCause();
        if (cause != null) {
            if (cause.getMessage() != null) {
                return cause.getMessage();
            } else if (cause.getCause() != null && cause.getCause().getMessage() != null) {
                return cause.getCause().getMessage();
            }
        }
        return "Unknown exception: " + exception.getMessage();
    }
}
