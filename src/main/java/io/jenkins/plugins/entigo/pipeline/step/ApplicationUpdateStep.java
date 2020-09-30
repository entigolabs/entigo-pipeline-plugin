package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSource;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.service.GitService;
import io.jenkins.plugins.entigo.pipeline.util.GitLockQueue;
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

    // TODO Add missing parameters
    private final String applicationName;
    private Boolean autoDeploy = false;

    @DataBoundConstructor
    public ApplicationUpdateStep(@CheckForNull String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
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

        private static final long serialVersionUID = 1; // Required by spotbugs

        private final transient ApplicationUpdateStep step;

        protected ApplicationUpdateStepExecution(@Nonnull StepContext context, ApplicationUpdateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            EnvVars envVars = getContext().get(EnvVars.class);
            FilePath workspace = getContext().get(FilePath.class);
            if (workspace == null) {
                throw new AbortException("Build Workspace is null");
            }
            listener.getLogger().println("Updating ArgoCD application...");
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    envVars);
            ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
            Application application = argoCDService.getApplication(step.applicationName);
            if (application == null) {
                // TODO Deploy new Application if autoDeploy true
            }
            // TODO What folder to use for git?
            ApplicationSource source = getApplicationSource(application);
            FilePath repoPath = workspace.child(step.applicationName);
            listener.getLogger().println("Using git repo location " + repoPath.toURI());
            // TODO Are git credentials global or job based?
            String credentialsId = PluginConfiguration.get().getGitCredentialsId();
            GitService gitService = new GitService(listener, envVars, repoPath, credentialsId, source.getRepoURL(),
                    source.getTargetRevision());
            GitLockQueue.executeInQueue(gitService.getGitBranch(), listener, () -> {
                gitService.checkout();
                // TODO File modifications from util
                repoPath.child(source.getPath()).createTextTempFile("test", ".txt", "testing-push-from-plugin");
                gitService.push("Pushing a test file from plugin");
                return null;
            });
            // TODO Sync app after pushing the new image version
            return null;
        }

        private ApplicationSource getApplicationSource(Application application) throws AbortException {
            ApplicationSource source = application.getSpec().getSource();
            if (source == null) {
                throw new AbortException("ArgoCD application response didn't include source");
            } else {
                return source;
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
            return ImmutableSet.of(TaskListener.class, Run.class, EnvVars.class, FilePath.class);
        }

        @Override
        public String getFunctionName() {
            return "updateArgoApp";
        }

        public FormValidation doCheckApplicationName(@QueryParameter String value) {
            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Application name is required");
            }
            return FormValidation.ok();
        }
    }
}
