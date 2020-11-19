package io.jenkins.plugins.entigo.pipeline.util;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

/**
 * Author: Märt Erlenheim
 * Date: 2020-10-05
 */
public class FormValidationUtil {

    private FormValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static FormValidation doCheckRequiredField(String value, String message) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.error(message);
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckTimeout(String value, Long lowerLimit, Long upperLimit, boolean required) {
        if (StringUtils.isEmpty(value)) {
            if (required) {
                return FormValidation.error("Timeout must be a positive number");
            } else {
                return FormValidation.ok();
            }
        }
        try {
            long timeout = Long.parseLong(value);
            if (timeout < lowerLimit || timeout > upperLimit) {
                return FormValidation.error(String.format("Timeout must be between %d and %d", lowerLimit, upperLimit));
            }
        } catch (NumberFormatException exception) {
            return FormValidation.error("Timeout must be a positive number");
        }
        return FormValidation.ok();
    }

}
