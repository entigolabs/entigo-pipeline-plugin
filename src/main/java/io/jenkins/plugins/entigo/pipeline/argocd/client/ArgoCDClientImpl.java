package io.jenkins.plugins.entigo.pipeline.argocd.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import hudson.cli.NoCheckTrustManager;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.rest.*;
import io.jenkins.plugins.entigo.pipeline.util.ProcessingExceptionUtil;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public class ArgoCDClientImpl implements ArgoCDClient {

    private static final String ARGOCD_API_PATH = "api/v1/";
    private static final Long DEFAULT_CONNECT_TIMEOUT = 30000L;
    private static final Long DEFAULT_READ_TIMEOUT = 30000L;

    private final Client restClient;
    private final WebTarget apiTarget;

    public ArgoCDClientImpl(String argoUri, String authToken, boolean ignoreCertificateErrors) throws ClientException {
        this.restClient = buildClient(ignoreCertificateErrors);
        this.apiTarget = restClient.target(UriBuilder.fromUri(argoUri).path(ARGOCD_API_PATH))
                .register(new Oauth2AuthenticationFilter(authToken));
    }

    private Client buildClient(boolean ignoreCertificateErrors) throws ClientException {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .register(JacksonJsonProvider.class)
                .register(JacksonConfiguration.class);

        if (ignoreCertificateErrors) {
            disableCertificateErrors(clientBuilder);
        }

        return clientBuilder.build();
    }

    private void disableCertificateErrors(ClientBuilder clientBuilder) throws ClientException {
        try {
            TrustManager[] trustManager = new X509TrustManager[] { new NoCheckTrustManager() };
            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, trustManager, null);
            clientBuilder.sslContext(sslcontext)
                    .hostnameVerifier((s1, s2) -> true);
        } catch (GeneralSecurityException e) {
            throw new ClientException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        restClient.close();
    }

    @Override
    public UserInfo getUserInfo() {
        return getRequest("session/userinfo", UserInfo.class);
    }

    @Override
    public Application getApplication(String applicationName, String projectName) {
        Map<String, Object> queryParams = projectName == null ? null : Collections.singletonMap("project", projectName);
        try {
            return getRequest("applications/{name}", Application.class,
                    Collections.singletonMap("name", applicationName), queryParams);
        } catch (ArgoCDException exception) {
            if (exception.getCode() == 5) {
                throw new NotFoundException(String.format("Application %s not found", applicationName));
            } else {
                throw exception;
            }
        }
    }

    @Override
    public Application syncApplication(String applicationName, ApplicationSyncRequest request) {
        try {
            return postRequest("applications/{name}/sync", Application.class, request,
                    Collections.singletonMap("name", applicationName), null);
        } catch (ArgoCDException exception) {
            if (exception.getCode() == 9) {
                throw new RetryableException(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public Response watchApplication(String applicationName, Integer readTimeout) {
        Map<String, Object> properties = new HashMap<>();
        if (readTimeout != null && readTimeout > 0) {
            properties.put(ClientProperties.READ_TIMEOUT, readTimeout);
        }
        return doRequest(HttpMethod.GET, "stream/applications", Response.class, null, Collections.emptyMap(),
                Collections.singletonMap("name", applicationName), properties);
    }

    @Override
    public void deleteApplication(String applicationName, boolean cascade) {
        doRequest(HttpMethod.DELETE, "applications/{name}", Void.class, null,
                Collections.singletonMap("name", applicationName), Collections.singletonMap("cascade", cascade));
    }

    private <T> T getRequest(String path, Class<T> responseType) {
        return getRequest(path, responseType, Collections.emptyMap(), null);
    }

    private <T> T getRequest(String path, Class<T> responseType, Map<String, Object> uriParams,
                             Map<String, Object> queryParams) {
        return doRequest(HttpMethod.GET, path, responseType, null, uriParams, queryParams);
    }

    private <T> T postRequest(String path, Class<T> responseType, Object request, Map<String, Object> uriParams,
                              Map<String, Object> queryParams) {
        return doRequest(HttpMethod.POST, path, responseType, request, uriParams, queryParams);
    }

    private <T> T doRequest(String method, String path, Class<T> responseType, Object request,
                            Map<String, Object> uriParams, Map<String, Object> queryParams) {
        return doRequest(method, path, responseType, request, uriParams, queryParams, null);
    }

    private <T> T doRequest(String method, String path, Class<T> responseType, Object request,
                            Map<String, Object> uriParams, Map<String, Object> queryParams,
                            Map<String, Object> properties) {
        try {
            WebTarget target = apiTarget.path(path).resolveTemplates(uriParams);
            target = setQueryParams(target, queryParams);
            setRequestProperties(target, properties);
            return target.request(MediaType.APPLICATION_JSON).method(method, Entity.json(request), responseType);
        } catch (WebApplicationException exception) {
            // Mostly HTTP errors like 403, 404 etc
            throw getResponseException(exception);
        } catch (ProcessingException exception) {
            // Mostly IO exceptions
            throw new RetryableException(ProcessingExceptionUtil.getExceptionMessage(exception), exception);
        }
    }

    private WebTarget setQueryParams(WebTarget target, Map<String, Object> queryParams) {
        if (queryParams != null) {
            for (Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
                target = target.queryParam(queryParam.getKey(), queryParam.getValue());
            }
        }
        return target;
    }

    private void setRequestProperties(WebTarget target, Map<String, Object> properties) {
        if (properties != null) {
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                target.property(property.getKey(), property.getValue());
            }
        }
    }

    private ResponseException getResponseException(WebApplicationException exception) {
        Response response = exception.getResponse();
        if (response != null && response.hasEntity()) {
            try {
                ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
                if (StringUtils.isNotEmpty(errorResponse.getError())) {
                    return new ArgoCDException(errorResponse.getError(), errorResponse.getCode());
                }
            } catch (ProcessingException readException) {
                throw new ArgoCDException("Failed to parse ArgoCD error response, message: " + exception.getMessage(),
                        -1, exception);
            }
        }
        return new ResponseException(exception.getMessage(), exception);
    }
}
