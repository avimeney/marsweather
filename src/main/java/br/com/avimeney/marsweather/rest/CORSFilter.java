package br.com.avimeney.marsweather.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Cross Origin Resource Sharing Filter. The Mars Weather Service API might be running in a server that
 * is not the same Web server responsible for the client interface. This filter is responsible for
 * adding a <code>Access-Control-Allow-Origin</code> header to the API's HTTP response in order to
 * guarantee cross origin access. 
 * 
 * @author avimeney
 */

@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		response.getHeaders().add("Access-Control-Allow-Origin", "*");
		// Authorization header must be explicitly allowed due to the presence of the auth token
		response.getHeaders().add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
    }
}
