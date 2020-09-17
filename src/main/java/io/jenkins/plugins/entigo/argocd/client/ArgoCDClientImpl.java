package io.jenkins.plugins.entigo.argocd.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.cli.NoCheckTrustManager;
import io.jenkins.plugins.entigo.argocd.model.ApplicationSyncRequest;
import io.jenkins.plugins.entigo.argocd.model.ApplicationWatchEvent;
import io.jenkins.plugins.entigo.argocd.model.ErrorResponse;
import io.jenkins.plugins.entigo.argocd.model.UserInfo;
import io.jenkins.plugins.entigo.rest.ClientException;
import io.jenkins.plugins.entigo.rest.JacksonConfiguration;
import io.jenkins.plugins.entigo.rest.Oauth2AuthenticationFilter;
import io.jenkins.plugins.entigo.rest.ResponseException;
import io.jenkins.plugins.entigo.util.ProcessingExceptionUtil;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ChunkedInput;

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
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public class ArgoCDClientImpl implements ArgoCDClient {

    private static final String ARGOCD_API_PATH = "api/v1/";

    private final Client restClient;
    private final WebTarget apiTarget;

    public ArgoCDClientImpl(String argoUri, String authToken, boolean ignoreCertificateErrors) {
        this.restClient = buildClient(ignoreCertificateErrors);
        this.apiTarget = restClient.target(UriBuilder.fromUri(argoUri).path(ARGOCD_API_PATH))
                .register(new Oauth2AuthenticationFilter(authToken));
    }

    private Client buildClient(boolean ignoreCertificateErrors) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder()
                .register(JacksonJsonProvider.class)
                .register(JacksonConfiguration.class);

        if (ignoreCertificateErrors) {
            disableCertificateErrors(clientBuilder);
        }

        return clientBuilder.build();
    }

    private void disableCertificateErrors(ClientBuilder clientBuilder) {
        try {
            TrustManager[] trustManager = new X509TrustManager[] { new NoCheckTrustManager() };
            SSLContext sslcontext = SSLContext.getInstance("SSL");
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
    public void syncApplication(String applicationName, ApplicationSyncRequest request) {
        // Have to use Void response type or else resteasy won't throw Not Found and Forbidden exceptions
        postRequest("applications/{name}/sync", Void.class, request,
                Collections.singletonMap("name", applicationName), null);
    }

    @Override
    @NonNull
    public ChunkedInput<ApplicationWatchEvent> watchApplication(String applicationName) {
        Response response = getRequest("stream/applications", Response.class, Collections.emptyMap(),
                Collections.singletonMap("name", applicationName));
        final ChunkedInput<ApplicationWatchEvent> input = response.readEntity(
                new GenericType<ChunkedInput<ApplicationWatchEvent>>() {}
        );
        input.setParser(ChunkedInput.createParser("\n"));
        return input;
    }

    private <T> T getRequest(String path, Class<T> responseType) {
        return getRequest(path, responseType, Collections.emptyMap(), null);
    }

    private <T> T getRequest(String path, Class<T> responseType, Map<String, Object> uriParams,
                             Map<String, Object> queryParams) {
        return doRequest(HttpMethod.GET, path, responseType, null, uriParams, queryParams);
    }

//    private <T> T postRequest(String path, Class<T> responseType, Object request) {
//        return postRequest(path, responseType, request, Collections.emptyMap(), null);
//    }

    private <T> T postRequest(String path, Class<T> responseType, Object request, Map<String, Object> uriParams,
                              Map<String, Object> queryParams) {
        return doRequest(HttpMethod.POST, path, responseType, request, uriParams, queryParams);
    }

    private <T> T doRequest(String method, String path, Class<T> responseType, Object request,
                            Map<String, Object> uriParams, Map<String, Object> queryParams) throws ResponseException {
        try {
            WebTarget target = apiTarget.path(path).resolveTemplates(uriParams);
            if (queryParams != null) {
                for (Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
                    target.queryParam(queryParam.getKey(), queryParam.getValue());
                }
            }
            return target.request(MediaType.APPLICATION_JSON).method(method, Entity.json(request), responseType);
        } catch (WebApplicationException exception) {
            Response response = exception.getResponse();
            if (response != null && response.hasEntity()) {
                try {
                    ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
                    if (StringUtils.isNotEmpty(errorResponse.getError())) {
                        throw new ResponseException(errorResponse.getError());
                    }
                } catch (ProcessingException readException) {
                    // Failed to parse json entity, probably caused by wrong content-type
                }
            }
            throw new ResponseException(exception.getMessage(), exception);
        } catch (ProcessingException exception) {
            throw new ResponseException(ProcessingExceptionUtil.getExceptionMessage(exception), exception);
        }
    }
}
