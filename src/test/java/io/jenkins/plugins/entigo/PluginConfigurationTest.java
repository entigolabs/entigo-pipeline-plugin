package io.jenkins.plugins.entigo;

import com.gargoylesoftware.htmlunit.html.*;
import io.jenkins.plugins.entigo.argocd.config.ArgoCDConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.junit.Assert.*;

public class PluginConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    @Test
    public void uiAndStorage() {
        rr.then(r -> {
            assertNull("initially null", PluginConfiguration.get().getArgoCDConfiguration());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
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
            ArgoCDConfiguration argoCDConfiguration = PluginConfiguration.get().getArgoCDConfiguration();
            assertNotNull("must be saved", argoCDConfiguration);
            assertEquals("https://localhost", argoCDConfiguration.getUri());
            assertEquals("argoCD", argoCDConfiguration.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConfiguration.getAppWaitTimeout());
            assertTrue(argoCDConfiguration.isIgnoreCertificateErrors());
        });
        rr.then(r -> {
            ArgoCDConfiguration argoCDConfiguration = PluginConfiguration.get().getArgoCDConfiguration();
            assertNotNull("must be present after restart", argoCDConfiguration);
            assertEquals("https://localhost", argoCDConfiguration.getUri());
            assertEquals("argoCD", argoCDConfiguration.getCredentialsId());
            assertEquals(Long.valueOf(500), argoCDConfiguration.getAppWaitTimeout());
            assertTrue(argoCDConfiguration.isIgnoreCertificateErrors());
        });
    }

}
