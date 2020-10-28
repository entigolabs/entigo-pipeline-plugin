package io.jenkins.plugins.entigo.pipeline.util;

import java.util.logging.Logger;
import hudson.model.TaskListener;

/**
 * Author: Märt Erlenheim
 * Date: 2020-10-28
 */
public class ListenerUtil {

    private static final Logger LOGGER = Logger.getLogger(ListenerUtil.class.getName());

    public static void println(TaskListener listener, String message) {
        if (listener == null) {
            LOGGER.fine("Listener is null, message to print: " + message);
        } else {
            listener.getLogger().println(message);
        }
    }
}
