package io.jenkins.plugins.entigo.pipeline.util;

import hudson.console.ConsoleLogFilter;
import hudson.console.LineTransformationOutputStream;
import hudson.model.Run;
import hudson.util.Secret;
import org.jenkinsci.plugins.credentialsbinding.masking.SecretPatternFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Author: Märt Erlenheim
 * Date: 2021-05-03
 */
public class SecretsFilter extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1;

    private static final Comparator<String> BY_LENGTH_DESCENDING =
            Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo);

    private final Secret pattern;
    private final String charsetName;

    public SecretsFilter(Collection<String> secrets, String charsetName) {
        this.pattern = createPattern(secrets);
        if (charsetName == null) {
            this.charsetName = StandardCharsets.UTF_8.name();
        } else {
            this.charsetName = charsetName;
        }
    }

    // Taken from credentials-binding plugin
    private Secret createPattern(Collection<String> secrets) {
        String pattern = secrets.stream()
                .filter(input -> !input.isEmpty())
                .flatMap(input ->
                        SecretPatternFactory.all().stream().flatMap(factory ->
                                factory.getEncodedForms(input).stream()))
                .sorted(BY_LENGTH_DESCENDING)
                .distinct()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Secret.fromString(Pattern.compile(pattern).pattern());
    }

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
