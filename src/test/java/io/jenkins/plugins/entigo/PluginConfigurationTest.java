package io.jenkins.plugins.entigo;

import com.gargoylesoftware.htmlunit.html.*;
import io.jenkins.plugins.entigo.argocd.config.ArgoCDConnection;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.util.Collections;

import static org.junit.Assert.*;

public class PluginConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    @Test
    public void uiAndStorage() {
        rr.then(r -> {
            assertEquals(0, PluginConfiguration.get().getArgoCDConnections().size());
            // Adds an empty connection which will be populated through the UI
            PluginConfiguration.get().setArgoCDConnections(Collections.singletonList(
                    new ArgoCDConnection(null, null, null)));
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput nameTextBox = config.getInputByName("_.name");
            nameTextBox.setText("localhost");
            HtmlTextInput uriTextBox = config.getInputByName("_.uri");
            uriTextBox.setText("https://localhost");
            HtmlSelect credentialsIdSelect = config.getSelectByName("_.credentialsId");
            HtmlOption option = credentialsIdSelect.getOptionByValue("");
            option.setValueAttribute("argoCD");
            credentialsIdSelect.setSelectedAttribute(option, true);
            HtmlCheckBoxInput ignoreSSL = config.getInputByName("_.ignoreCertificateErrors");
            ignoreSSL.setChecked(true);
            HtmlNumberInput timeoutInput = config.getInputByName("_.appWaitTimeout");
            timeoutInput.setText("500");
            r.submit(config);
            ArgoCDConnection argoCDConnection = PluginConfiguration.get().getArgoCDConnection("localhost");
            assertNotNull("must be saved", argoCDConnection);
            assertEquals("https://localhost", argoCDConnection.getUri());
            assertEquals("argoCD", argoCDConnection.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConnection.getAppWaitTimeout());
            assertTrue(argoCDConnection.isIgnoreCertificateErrors());
        });
        rr.then(r -> {
            ArgoCDConnection argoCDConnection = PluginConfiguration.get().getArgoCDConnection("localhost");
            assertNotNull("must be present after restart", argoCDConnection);
            assertEquals("https://localhost", argoCDConnection.getUri());
            assertEquals("argoCD", argoCDConnection.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConnection.getAppWaitTimeout());
            assertTrue(argoCDConnection.isIgnoreCertificateErrors());
        });
    }

}
