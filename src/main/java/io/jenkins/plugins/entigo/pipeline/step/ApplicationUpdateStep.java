package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSource;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-14
 */
public class ApplicationUpdateStep extends Step {

    private final String name;
    private Boolean autoDeploy = false;

    @DataBoundConstructor
    public ApplicationUpdateStep(@CheckForNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Boolean getAutoDeploy() {
        return autoDeploy;
    }

    @DataBoundSetter
    public void setAutoDeploy(Boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new ApplicationUpdateStep.ApplicationUpdateStepExecution(stepContext, this);
    }

    public static class ApplicationUpdateStepExecution extends SynchronousStepExecution<Void> {

        private final transient ApplicationUpdateStep step;

        protected ApplicationUpdateStepExecution(@Nonnull StepContext context, ApplicationUpdateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            listener.getLogger().println("Updating ArgoCD application...");
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    getContext().get(EnvVars.class));
            ArgoCDClient client = connection.getClient();
            Application application = client.getApplication(step.name);
            if (application == null) {
                // TODO Deploy new Application
            } else {
                // TODO Update application
            }
            return null;
        }

        private String getRepoUrl(Application application) throws AbortException {
            ApplicationSource source = application.getSpec().getSource();
            if (source == null) {
                throw new AbortException("ArgoCD application response didn't include source");
            } else {
                String repoUrl = source.getRepoURL();
                if (repoUrl == null) {
                    throw new AbortException("RepoUrl is missing in ArgoCD response");
                } else {
                    return repoUrl;
                }
            }
        }

    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Update ArgoCD application";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, Run.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "updateArgoApp";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Application name is required");
            }
            return FormValidation.ok();
        }
    }
}
