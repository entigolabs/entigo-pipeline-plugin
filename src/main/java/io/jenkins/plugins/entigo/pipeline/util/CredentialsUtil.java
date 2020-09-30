package io.jenkins.plugins.entigo.pipeline.util;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.AbortException;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-29
 */
public class CredentialsUtil {

    private CredentialsUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T extends StandardCredentials> FormValidation checkCredentialsId(
            Item context, String value, Class<T> credentialsClass, List<DomainRequirement> domainRequirements) {
        if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
                context != null && !context.hasPermission(Item.EXTENDED_READ) &&
                        !context.hasPermission(CredentialsProvider.USE_ITEM)) {
            return FormValidation.ok();
        }

        if (StringUtils.isEmpty(value)) {
            return FormValidation.error("Credentials are required.");
        }

        if (CredentialsProvider.listCredentials(
                credentialsClass,
                context,
                ACL.SYSTEM,
                domainRequirements,
                CredentialsMatchers.withId(value)
        ).isEmpty()) {
            return FormValidation.error("Cannot find currently selected credentials");
        }

        return FormValidation.ok();
    }

    public static <T extends StandardCredentials> T findCredentialsById(
            String credentialsId, Class<T> credentialsClass, List<DomainRequirement> domainRequirements)
            throws AbortException {
        T credentials = CredentialsMatchers.firstOrNull(
                lookupCredentials(
                        credentialsClass,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        domainRequirements
                ), CredentialsMatchers.withId(credentialsId));
        if (credentials == null) {
            throw new AbortException(String.format("No credentials found with credentialsId %s and type %s",
                    credentialsId, credentialsClass.getName()));
        } else {
            return credentials;
        }
    }
}
