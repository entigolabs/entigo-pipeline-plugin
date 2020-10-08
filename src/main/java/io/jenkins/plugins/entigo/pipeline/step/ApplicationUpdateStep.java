package io.jenkins.plugins.entigo.pipeline.step;

import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import io.jenkins.plugins.entigo.pipeline.argocd.ArgoCDMapper;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import io.jenkins.plugins.entigo.pipeline.argocd.model.Application;
import io.jenkins.plugins.entigo.pipeline.argocd.model.ApplicationSource;
import io.jenkins.plugins.entigo.pipeline.argocd.service.ArgoCDService;
import io.jenkins.plugins.entigo.pipeline.config.DeploymentArguments;
import io.jenkins.plugins.entigo.pipeline.service.GitService;
import io.jenkins.plugins.entigo.pipeline.util.FormValidationUtil;
import io.jenkins.plugins.entigo.pipeline.util.GitLockQueue;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-14
 */
public class ApplicationUpdateStep extends Step {

    private final String applicationName;
    private final String projectName;
    private final String imageName;
    private final String imageVersion;
    private Boolean autoDeploy = false;
    private DeploymentArguments deploymentArgs;

    @DataBoundConstructor
    public ApplicationUpdateStep(@CheckForNull String applicationName, @CheckForNull String projectName,
                                 @CheckForNull String imageName, @CheckForNull String imageVersion) {
        this.applicationName = applicationName;
        this.projectName = projectName;
        this.imageName = imageName;
        this.imageVersion = imageVersion;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public Boolean getAutoDeploy() {
        return autoDeploy;
    }

    @DataBoundSetter
    public void setAutoDeploy(Boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    public DeploymentArguments getDeploymentArgs() {
        return deploymentArgs;
    }

    @DataBoundSetter
    public void setDeploymentArgs(DeploymentArguments deploymentArgs) {
        this.deploymentArgs = deploymentArgs;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new ApplicationUpdateStep.ApplicationUpdateStepExecution(stepContext, this);
    }

    public static class ApplicationUpdateStepExecution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1; // Required by spotbugs

        private final transient ApplicationUpdateStep step;
        private final transient ArgoCDMapper argoCDMapper;

        protected ApplicationUpdateStepExecution(@Nonnull StepContext context, ApplicationUpdateStep step) {
            super(context);
            this.step = step;
            this.argoCDMapper = ArgoCDMapper.get();
        }

        @Override
        protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            EnvVars envVars = getContext().get(EnvVars.class);
            listener.getLogger().println("Updating ArgoCD application...");
            ArgoCDConnection connection = ArgoCDConnectionsProperty.getConnection(getContext().get(Run.class),
                    envVars);
            ArgoCDService argoCDService = new ArgoCDService(connection.getClient());
            Application application = getApplication(argoCDService);
            updateRepositoryDeployment(listener, envVars, application);
            if (application.getStatus() == null) {
                listener.getLogger().println("Deploying new application to ArgoCD");
                argoCDService.postApplication(application);
            }
            argoCDService.syncApplicationWithWait(listener, step.applicationName, connection.getAppWaitTimeout());
            return null;
        }

        private Application getApplication(ArgoCDService argoCDService) throws AbortException {
            Application application = argoCDService.getApplication(step.applicationName, step.projectName);
            if (application == null && step.autoDeploy) {
                application = createApplication();
            } else {
                throw new AbortException("ArgoCD application not found and auto deploy is disabled, name " +
                        step.applicationName);
            }
            return application;
        }

        private Application createApplication() throws AbortException {
            DeploymentArguments arguments = step.deploymentArgs;
            if (arguments == null) {
                throw new AbortException("Deployment arguments are missing, can't create an application");
            }
            return argoCDMapper.createApplication(step.applicationName, step.projectName, arguments.getRepoUrl(),
                    arguments.getTemplatePath(), arguments.getTargetRevision(), arguments.getClusterUrl(),
                    arguments.getTargetNamespace());
        }

        private void updateRepositoryDeployment(TaskListener listener, EnvVars envVars, Application application)
                throws Exception {
            FilePath repoWorkSpace = getRepoWorkSpace();
            listener.getLogger().println("Using git repo location " + repoWorkSpace.toURI());
            ApplicationSource source = getApplicationSource(application);
            String credentialsId = PluginConfiguration.get().getGitCredentialsId();
            GitService gitService = new GitService(listener, envVars, repoWorkSpace, credentialsId, source.getRepoURL(),
                    source.getTargetRevision());
            GitLockQueue.executeInQueue(gitService.getGitBranch(), listener, () -> {
                gitService.checkout();
                // TODO File modifications from util container
                // TODO What files to add?
                gitService.push("Updated deployment yaml for " + step.applicationName, Collections.singletonList("."));
                return null;
            });
        }

        private FilePath getRepoWorkSpace() throws IOException, InterruptedException {
            FilePath workspace = getContext().get(FilePath.class);
            if (workspace == null) {
                throw new AbortException("Build Workspace is null");
            }
            return workspace.child(step.projectName + "/" + step.applicationName);
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
            return FormValidationUtil.doCheckRequiredField(value, "Application name is required");
        }

        public FormValidation doCheckProjectName(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Project name is required");
        }

        public FormValidation doCheckImageName(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Image name is required");
        }

        public FormValidation doCheckImageVersion(@QueryParameter String value) {
            return FormValidationUtil.doCheckRequiredField(value, "Image version is required");
        }

        public FormValidation doCheckAutoDeploy(@QueryParameter boolean value,
                                                @QueryParameter boolean deploymentArgs) {
            if (value) {
                // Using custom objects in validation methods causes unable to convert exception
                // Boolean workaround enables to still check if the object is set or not
                if (!deploymentArgs) {
                    return FormValidation.error("Deployment arguments must be set for auto deployment");
                }
            }
            return FormValidation.ok();
        }
    }
}
