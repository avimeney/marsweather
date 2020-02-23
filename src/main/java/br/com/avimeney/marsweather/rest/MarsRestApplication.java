package br.com.avimeney.marsweather.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Entry point for the Mars Weather API REST Server. 
 *  
 * @author avimeney
 */

public class MarsRestApplication {
	/*
	 * Keys and default server configuration values.
	 * The same keys must be found at the application.properties file.
	 */
    private static final String BASE_URI_KEY = "BASE_URI";
    private static final String DEFAULT_BASE_URI = "http://localhost:8080/marsweather/";
    
    public static final String API_AUTH_TOKEN_KEY = "API_AUTH_TOKEN";
    private static final String DEFAULT_API_AUTH_TOKEN = "PREODAY_TEST_TOKEN";
    
    public static final String CACHE_INVALIDATION_PERIOD_KEY = "CACHE_INVALIDATION_PERIOD";
    public static final String DEFAULT_CACHE_INVALIDATION_PERIOD = "4";
    
    public static final String NASA_API_URI_KEY = "NASA_API_URI";
    private static final String DEFAULT_NASA_API_URI = "https://api.nasa.gov/insight_weather/";
    
    public static final String NASA_API_KEY_KEY = "NASA_API_KEY";
    private static final String DEFAULT_NASA_API_KEY = "DEMO_KEY";
    
	private static final Logger logger = LogManager.getLogger(MarsRestApplication.class);
	
	/**
	 * Default server configuration.
	 * 
	 * @see #appProperties
	 */
	private static final Properties defaultProperties = new Properties();
	
	/**
	 * Server configuration. Based on {@link #defaultProperties} and overridden by user
	 * defined configuration through application.properties. 
	 */
	private static Properties appProperties;

	static {
		/*
		 * Loading the default configuration properties into the Properties object: 
		 */
		defaultProperties.setProperty(BASE_URI_KEY, DEFAULT_BASE_URI);
		defaultProperties.setProperty(API_AUTH_TOKEN_KEY, DEFAULT_API_AUTH_TOKEN);
		defaultProperties.setProperty(CACHE_INVALIDATION_PERIOD_KEY, DEFAULT_CACHE_INVALIDATION_PERIOD);
		defaultProperties.setProperty(NASA_API_URI_KEY, DEFAULT_NASA_API_URI);
		defaultProperties.setProperty(NASA_API_KEY_KEY, DEFAULT_NASA_API_KEY);
	}
	
	public static Properties getAppProperties() {
		return appProperties;
	}
	
	public static void main(String[] args) throws IOException {
		
		logger.info("Mars Weather Server starting");
		/*
		 * Loading user defined server configuration stored at application.properties.
		 */
		try {
			logger.debug("Reading application properties");
			final InputStream stream = MarsRestApplication.class.getResourceAsStream("/application.properties");
			appProperties = new Properties(defaultProperties);
			appProperties.load(stream);
			logger.debug("Application properties read!");
			if (!appProperties.isEmpty()) {
				logger.debug(appProperties);
			}
		} catch (IOException e) {
			logger.error("Failure while trying to load application propertires.", e);
			throw e;
		}
		/*
		 * REST services are annotated at rest package. 
		 */
    	final ResourceConfig resourceConfig = new ResourceConfig().packages("br.com.avimeney.marsweather.rest");
    	/*
    	 * Enabling Jackson for JSON marshaling. Check for annotated classes at the model package.  
    	 */
    	resourceConfig.register(JacksonFeature.class);
    	/*
    	 * Starting Grizzly HTTP embedded server at the user specified URI:
    	 */
    	GrizzlyHttpServerFactory.createHttpServer(URI.create(appProperties.getProperty(BASE_URI_KEY)), resourceConfig);
    	logger.info("Mars Weather Server available at "+appProperties.getProperty(BASE_URI_KEY));
    }
}