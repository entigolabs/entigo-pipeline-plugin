package io.jenkins.plugins.entigo.pipeline.step;

import hudson.Extension;
import io.jenkins.plugins.entigo.pipeline.PluginConfiguration;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Märt Erlenheim
 * Date: 2020-11-19
 */
public class ListArgoConnectionsStep extends Step {

    @DataBoundConstructor
    public ListArgoConnectionsStep() {
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new ListArgoConnectionsStepExecution(stepContext);
    }

    public static class ListArgoConnectionsStepExecution extends SynchronousStepExecution<List<String>> {

        private static final long serialVersionUID = 1;

        protected ListArgoConnectionsStepExecution(@Nonnull StepContext context) {
            super(context);
        }

        @Override
        protected List<String> run() {
            PluginConfiguration configuration = PluginConfiguration.get();
            // keySet is not serializable so can't be returned from step, sorted for better readability
            return configuration.getNamedArgoCDConnections().keySet().stream().sorted().collect(Collectors.toList());
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "List ArgoCD connections";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }

        @Override
        public String getFunctionName() {
            return "listArgoConnections";
        }
    }
}
