package io.jenkins.plugins.entigo.rest;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-25
 */
public class Oauth2AuthenticationFilter implements ClientRequestFilter {

    private final String oauth2Header;

    public Oauth2AuthenticationFilter(String token) {
        this.oauth2Header = "Bearer " + token;
    }

    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle("Authorization", this.oauth2Header);
    }
}
