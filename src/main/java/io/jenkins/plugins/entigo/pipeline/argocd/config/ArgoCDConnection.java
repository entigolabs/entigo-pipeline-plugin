package io.jenkins.plugins.entigo.pipeline.argocd.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.AbortException;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClientBuilder;
import io.jenkins.plugins.entigo.pipeline.argocd.model.UserInfo;
import io.jenkins.plugins.entigo.pipeline.rest.ResponseException;
import io.jenkins.plugins.entigo.pipeline.rest.ClientException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ArgoCDConnection extends AbstractDescribableImpl<ArgoCDConnection> {

    private final String name;
    private final String uri;
    private final String credentialsId;
    private boolean ignoreCertificateErrors = false;
    private Long appWaitTimeout = 300L;
    private transient ArgoCDClient client;

    @DataBoundConstructor
    public ArgoCDConnection(String name, String uri, String credentialsId) {
        this.name = name;
        this.uri = uri;
        this.credentialsId = credentialsId;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public boolean isIgnoreCertificateErrors() {
        return ignoreCertificateErrors;
    }

    @DataBoundSetter
    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
        this.ignoreCertificateErrors = ignoreCertificateErrors;
    }

    public Long getAppWaitTimeout() {
        return appWaitTimeout;
    }

    @DataBoundSetter
    public void setAppWaitTimeout(Long appWaitTimeout) {
        this.appWaitTimeout = appWaitTimeout;
    }

    public ArgoCDClient getClient() throws AbortException {
        if (client == null) {
            try {
                ArgoCDClientBuilder builder = ExtensionList.lookupSingleton(ArgoCDClientBuilder.class);
                if (ignoreCertificateErrors) {
                    client = builder.buildUnsecuredClient(uri, getApiToken());
                } else {
                    client = builder.buildSecuredClient(uri, getApiToken());
                }
            } catch (ClientException exception) {
                throw new AbortException("Failed to create an ArgoCD client, message: " + exception.getMessage());
            }
        }
        return client;
    }

    @Restricted(NoExternalUse.class)
    private String getApiToken() {
        StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                lookupCredentials(
                        StandardCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(uri).build()
                ), CredentialsMatchers.withId(credentialsId));
        if (credentials instanceof StringCredentials) {
            return ((StringCredentials) credentials).getSecret().getPlainText();
        }
        throw new IllegalStateException("No credentials found with credentialsId: " + credentialsId);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ArgoCDConnection> {

        public FormValidation doCheckName(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Name is required.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckUri(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Uri is required.");
            }
            URI uri = UriBuilder.fromUri(value).build();
            if (uri.getScheme() == null) {
                return FormValidation.error("Scheme (http or https) is missing");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String uri,
                                                   @QueryParameter String value) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Credentials are required.");
            }
            if (CredentialsProvider.listCredentials(
                    StandardCredentials.class,
                    item,
                    ACL.SYSTEM,
                    URIRequirementBuilder.fromUri(uri).build(),
                    CredentialsMatchers.withId(value)
            ).isEmpty()) {
                return FormValidation.error("Cannot find currently selected credentials");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAppWaitTimeout(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Timeout is required.");
            }
            try {
                long timeout = Long.parseLong(value);
                if (timeout < 1L || timeout > 1800L) {
                    return FormValidation.error("Timeout must be between 1 and 1800");
                }
            } catch (NumberFormatException exception) {
                return FormValidation.error("Must be a positive number");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        @Restricted(DoNotUse.class)
        public FormValidation doTestConnection(@QueryParameter String name,
                                               @QueryParameter String uri,
                                               @QueryParameter String credentialsId,
                                               @QueryParameter boolean ignoreCertificateErrors) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            ArgoCDClient argoCDClient = null;
            try {
                ArgoCDConnection connection = new ArgoCDConnection(name, uri, credentialsId);
                connection.setIgnoreCertificateErrors(ignoreCertificateErrors);
                argoCDClient = connection.getClient();
                UserInfo userInfo = argoCDClient.getUserInfo();
                if (Boolean.TRUE.equals(userInfo.getLoggedIn())) {
                    return FormValidation.ok("Success, authenticated as " + userInfo.getUsername());
                } else {
                    return FormValidation.error("Successful request but user is not logged into ArgoCD");
                }
            } catch (ResponseException exception) {
                return FormValidation.error(exception.getMessage());
            } catch (AbortException e) {
                return FormValidation.error("Failed to create an api client ssl context, message:" + e.getMessage());
            } finally {
                if (argoCDClient != null) {
                    argoCDClient.close();
                }
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String uri,
                                                     @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            item,
                            StandardCredentials.class,
                            URIRequirementBuilder.fromUri(uri).build(),
                            CredentialsMatchers.always()
                    )
                    .includeCurrentValue(credentialsId);
        }
    }

}
