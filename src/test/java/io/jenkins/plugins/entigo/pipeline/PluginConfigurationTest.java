package io.jenkins.plugins.entigo.pipeline;

import com.gargoylesoftware.htmlunit.html.*;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnection;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionMatcher;
import io.jenkins.plugins.entigo.pipeline.argocd.config.ArgoCDConnectionsProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.util.Collections;

import static org.junit.Assert.*;

public class PluginConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    @Test
    public void configure_ArgoCDConnections_ShouldSaveAndLoad() {
        rr.then(r -> {
            assertEquals(0, PluginConfiguration.get().getArgoCDConnections().size());
            // Adds an empty connection which will be populated through the UI
            PluginConfiguration.get().setArgoCDConnections(Collections.singletonList(
                    new ArgoCDConnection(null, null, null)));
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlSelect defaultArgoCDConnectionSelect = config.getSelectByName("_.defaultArgoCDConnection");
            HtmlOption connectionOption = defaultArgoCDConnectionSelect.getOption(0);
            connectionOption.setValueAttribute("localConnection");
            defaultArgoCDConnectionSelect.setSelectedAttribute(connectionOption, true);
            HtmlTextInput nameTextBox = config.getInputByName("_.name");
            nameTextBox.setText("localhost");
            HtmlTextInput uriTextBox = config.getInputByName("_.uri");
            uriTextBox.setText("https://localhost");
            HtmlSelect credentialsIdSelect = config.getSelectByName("_.credentialsId");
            HtmlOption credentialOption = credentialsIdSelect.getOption(0);
            credentialOption.setValueAttribute("argoCD");
            credentialsIdSelect.setSelectedAttribute(credentialOption, true);
            HtmlCheckBoxInput ignoreSSL = config.getInputByName("_.ignoreCertificateErrors");
            ignoreSSL.setChecked(true);
            HtmlNumberInput timeoutInput = config.getInputByName("_.appWaitTimeout");
            timeoutInput.setText("500");
            r.submit(config);
            PluginConfiguration configuration = PluginConfiguration.get();
            assertEquals("localConnection", configuration.getDefaultArgoCDConnection());
            ArgoCDConnection argoCDConnection = configuration.getArgoCDConnection("localhost");
            assertNotNull("must be saved", argoCDConnection);
            assertEquals("https://localhost", argoCDConnection.getUri());
            assertEquals("argoCD", argoCDConnection.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConnection.getAppWaitTimeout());
            assertTrue(argoCDConnection.isIgnoreCertificateErrors());
        });
        rr.then(r -> {
            PluginConfiguration configuration = PluginConfiguration.get();
            assertEquals("localConnection", configuration.getDefaultArgoCDConnection());
            ArgoCDConnection argoCDConnection = configuration.getArgoCDConnection("localhost");
            assertNotNull("must be present after restart", argoCDConnection);
            assertEquals("https://localhost", argoCDConnection.getUri());
            assertEquals("argoCD", argoCDConnection.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConnection.getAppWaitTimeout());
            assertTrue(argoCDConnection.isIgnoreCertificateErrors());
        });
    }

    @Test
    public void configure_argoCDConnectionsProperty_ShouldSaveAndLoad() {
        rr.then(r -> {
            assertEquals(0, PluginConfiguration.get().getArgoCDConnections().size());
            // Need a connection for selection
            ArgoCDConnection connection = new ArgoCDConnection("connection", null, null);
            connection.setGenerateMatcher(false);
            PluginConfiguration.get().setArgoCDConnections(Collections.singletonList(connection));
            // Adds an empty connection which will be populated through the UI
            ArgoCDConnectionsProperty newProperty = new ArgoCDConnectionsProperty(
                    Collections.singleton(new ArgoCDConnectionMatcher(null, null)));
            PluginConfiguration.get().setArgoCDConnectionsProperty(newProperty);
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput nameTextBox = config.getInputByName("_.pattern");
            nameTextBox.setText("pattern");
            HtmlSelect credentialsIdSelect = config.getSelectByName("_.connectionName");
            HtmlOption option = credentialsIdSelect.getOption(0);
            option.setValueAttribute("name");
            r.submit(config);
            ArgoCDConnectionsProperty property = PluginConfiguration.get().getArgoCDConnectionsProperty();
            assertNotNull("must be saved", property);
            assertEquals(1, property.getMatchers().size());
            ArgoCDConnectionMatcher matcher = property.getMatchers().iterator().next();
            assertEquals("pattern", matcher.getPattern());
            assertEquals("name", matcher.getConnectionName());
        });
        rr.then(r -> {
            ArgoCDConnectionsProperty property = PluginConfiguration.get().getArgoCDConnectionsProperty();
            assertNotNull("must be present after restart", property);
            assertEquals(1, property.getMatchers().size());
            ArgoCDConnectionMatcher matcher = property.getMatchers().iterator().next();
            assertEquals("pattern", matcher.getPattern());
            assertEquals("name", matcher.getConnectionName());
        });
    }

}
