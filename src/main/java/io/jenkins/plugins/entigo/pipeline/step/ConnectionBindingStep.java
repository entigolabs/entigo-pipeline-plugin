package io.jenkins.plugins.entigo.pipeline.step;

import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hudson.model.Run;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.util.CredentialsUtil;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Author: Märt Erlenheim
 * Date: 2021-05-03
 */
public class ConnectionBindingStep extends Step {

    private static final String ARGO_CD_SERVER_ENV="ARGO_CD_SERVER";
    private static final String ARGO_CD_TOKEN_ENV="ARGO_CD_TOKEN";

    private final String connectionSelector;

    @DataBoundConstructor
    public ConnectionBindingStep(String connectionSelector) {
        this.connectionSelector = connectionSelector;
    }

    public String getConnectionSelector() {
        return connectionSelector;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(connectionSelector, context);
    }

    public static class Execution extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final String connectionSelector;

        Execution(String connectionSelector, StepContext context) {
            super(context);
            this.connectionSelector = connectionSelector;
        }

        @Override
        public boolean start() throws Exception {
            Map<String, String> connectionMap = new HashMap<>();
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class), connectionSelector);
            StringCredentials credentials = CredentialsUtil.findCredentialsById(connection.getCredentialsId(),
                    StringCredentials.class, URIRequirementBuilder.fromUri(connection.getUri()).build());
            connectionMap.put(ARGO_CD_SERVER_ENV, connection.getUri().replaceAll("(?i)^http[s]?://", ""));
            connectionMap.put(ARGO_CD_TOKEN_ENV, credentials.getSecret().getPlainText());
            getContext().newBodyInvoker().
                    withContext(EnvironmentExpander.merge(getContext().get(EnvironmentExpander.class),
                            EnvironmentExpander.constant(connectionMap))).
                    withCallback(BodyExecutionCallback.wrap(getContext())).
                    start();
            return false;
        }

        @Override
        public void onResume() {
        }

    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "withArgoCDConnection";
        }

        @Override
        public String getDisplayName() {
            return "Set ArgoCD connection variables";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

    }

}
