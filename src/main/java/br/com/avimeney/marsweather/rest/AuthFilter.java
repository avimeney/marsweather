package br.com.avimeney.marsweather.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Token based authentication filter. This simple implementation only requires the presence of
 * a authentication token corresponding to a pre defined value in the request header.
 * Future versions may evolve to a effective token-based authentication protocol, as the JWT.
 * 
 * <p>The valid token value is defined in the application.properties file by the
 * {@value MarsRestApplication#API_AUTH_TOKEN_KEY} key. 
 *  
 * @author avimeney
 */

@Priority(Priorities.AUTHENTICATION)
@Provider
public class AuthFilter implements ContainerRequestFilter, ContainerResponseFilter {

	/**
	 * The expected value for the authentication token.
	 */
	private final String allowedTokenValue;

	public AuthFilter() {
		allowedTokenValue = (String) MarsRestApplication.getAppProperties().getProperty(MarsRestApplication.API_AUTH_TOKEN_KEY);
	}

	@SuppressWarnings("serial")
	private class ValidationException extends Exception {
		public ValidationException(String msg) {
			super(msg);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		final String authHeaderVal = requestContext.getHeaderString("Authorization");

		if (requestContext.getMethod() != HttpMethod.OPTIONS) {
			if (authHeaderVal != null && authHeaderVal.startsWith("Bearer")) {
				try {
					validate(authHeaderVal.split(" ")[1]);
				} catch (ValidationException e) {
					requestContext.setProperty("authFailure", e.getMessage());
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				}
			} else {
				requestContext.setProperty("authFailure", "no auth token");
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}
	}

	private void validate(String authToken) throws ValidationException {
		if (!allowedTokenValue.equals(authToken)) {
			throw new ValidationException("invalid token");
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		if (requestContext.getMethod() != HttpMethod.OPTIONS) {
			final String authProperty = (String) requestContext.getProperty("authFailure");
			if (authProperty != null) {
				responseContext.getHeaders().add("WWW-Authenticate", "Bearer error=\""+authProperty+"\"");
			}
		}
	}
}
