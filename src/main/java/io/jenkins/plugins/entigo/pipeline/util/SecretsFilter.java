package io.jenkins.plugins.entigo.pipeline.util;

import hudson.console.ConsoleLogFilter;
import hudson.console.LineTransformationOutputStream;
import hudson.model.Run;
import hudson.util.Secret;
import org.jenkinsci.plugins.credentialsbinding.masking.SecretPatterns;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Märt Erlenheim
 * Date: 2021-05-03
 */
public class SecretsFilter extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1;

    private final Secret pattern;
    private String charsetName;

    public SecretsFilter(Collection<String> secrets, String charsetName) {
        pattern = Secret.fromString(SecretPatterns.getAggregateSecretPattern(secrets).pattern());
        if (charsetName == null) {
            this.charsetName = StandardCharsets.UTF_8.name();
        } else {
            this.charsetName = charsetName;
        }
    }

    // Taken from credentials-binding plugin
    @Override public OutputStream decorateLogger(Run _ignore, final OutputStream logger) {
        final Pattern p = Pattern.compile(pattern.getPlainText());
        return new LineTransformationOutputStream() {
            @Override protected void eol(byte[] b, int len) throws IOException {
                if (!p.toString().isEmpty()) {
                    Matcher m = p.matcher(new String(b, 0, len, charsetName));
                    if (m.find()) {
                        logger.write(m.replaceAll("****").getBytes(charsetName));
                    } else {
                        // Avoid byte → char → byte conversion unless we are actually doing something.
                        logger.write(b, 0, len);
                    }
                } else {
                    // Avoid byte → char → byte conversion unless we are actually doing something.
                    logger.write(b, 0, len);
                }
            }

            @Override public void flush() throws IOException {
                logger.flush();
            }

            @Override public void close() throws IOException {
                super.close();
                logger.close();
            }
        };
    }
}
