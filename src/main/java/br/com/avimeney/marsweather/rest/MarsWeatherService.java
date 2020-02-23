package br.com.avimeney.marsweather.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.com.avimeney.marsweather.integration.nasa.WeatherDTO;
import br.com.avimeney.marsweather.integration.nasa.WeatherJsonParser;
import br.com.avimeney.marsweather.model.SolData;

/**
 * Implements the Mars Weather Service REST API. The API defines a single method responsible
 * for providing the last temperature measurements available for the planet Mars.   
 * 
 * <p>The current implementation relies on the <i>NASA's InSight API, version {@value #NASA_API_VERSION}</i>
 * that provides the underlining raw atmospheric data that is encapsulated by the service implemented by this class.
 * This implementation filters the original data, discarding several atmospheric parameters and
 * preserving only the temperature data, which is the focus of the current implementation.
 * 
 * <p>This service implementation counts on a local cache to avoid excessive number of calls to the
 * underlining API. There are two major motivations for using this cache:   
 * 
 * <ol>
 *   <li>NASA imposes limitation on the usage of its API.
 *   <li>According to NASA's documentation, new weather data may take several days between being
 * acquired by the Mars spacecraft and being made available at its API. That means that consecutive
 * calls to the NASA's API are likely to return the same results.  
 * </ol>
 * 
 * <p>The cache implementation itself is pretty simple. The first call to this API will trigger a
 * call to the underlining NASA's API. The obtained data will be stored in the local cache.
 * Subsequent calls to this API will consume data directly from the local cache. A daemon thread
 * will periodically invalidate the entire cache. Once invalidated the next call will trigger a
 * new request to the NASA's API, bringing potentially updated data.
 * 
 * <p>The frequency of cache invalidations, as well as NASA's API parameters can be configured by
 * the user through the <i>application.properties</i> file. Consult the server user guide for
 * further information on the server setup.
 * 
 * @author avimeney
 */

@Path("api/v1/weather")
@Singleton
public class MarsWeatherService {
	
	private static final String NASA_API_VERSION = "1.0";

	private static final int HTTP_STATUS_SUCCESS = 200;

	private static final int HOURS_IN_MILLIS = 60*60*1000;
	
	private static final Logger logger = LogManager.getLogger(MarsRestApplication.class);

	/**
	 * Sol temperature data cache. Sol number is used as key and Sol temperature data as value.  
	 */
	private final Map<Integer, SolData> solDataMap = new HashMap<Integer, SolData>();
	
	/*
	 * Write and read locks for cache concurrency control.
	 */
	private final Lock writeLock;
	private final Lock readLock;

	/**
	 * Pre-built HTTP request for NASA's REST API.
	 */
	private final Builder nasaRequest;
	
	public MarsWeatherService(){
		logger.info("Mars Weather Service starting");
		/*
		 * Storing the necessary application properties in instance fields for further use: 
		 */
		final Properties appProperties = MarsRestApplication.getAppProperties();
		final String nasaKey = appProperties.getProperty(MarsRestApplication.NASA_API_KEY_KEY);
		final String nasaUri = appProperties.getProperty(MarsRestApplication.NASA_API_URI_KEY);
		/*
		 * Trying to honor the user specified cache invalidation period:  
		 */
		final String invPeriodString = appProperties.getProperty(MarsRestApplication.CACHE_INVALIDATION_PERIOD_KEY);
		int invalidationPeriod;
		try {
			invalidationPeriod = Integer.parseInt(invPeriodString) * HOURS_IN_MILLIS;
		} catch (NumberFormatException e) {
			logger.warn("Invalid invalidation period from application properties. Using default value instead.");
			invalidationPeriod = Integer.parseInt(MarsRestApplication.DEFAULT_CACHE_INVALIDATION_PERIOD) * HOURS_IN_MILLIS;
		}
		/*
		 * Building a request object that will be reused on every NASA' API access.
		 */
		final Client client = ClientBuilder.newBuilder().build();
		final WebTarget target = client.target(nasaUri
				).queryParam("api_key", nasaKey
				).queryParam("feedtype", "json"
				).queryParam("ver", NASA_API_VERSION);
		nasaRequest = target.request();
		/*
		 * Locks for cache concurrency control:
		 */
		final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		writeLock = readWriteLock.writeLock();
		readLock = readWriteLock.readLock();
		/*
		 * Cache invalidation daemon thread initialization:
		 */
		final Timer timer = new Timer("Cache Invalidation Timer", true);
		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				logger.debug("Invalidation thread running");
				writeLock.lock();
				solDataMap.clear();
				writeLock.unlock();
				logger.debug("Cache invalidated");
			}
		};
		timer.scheduleAtFixedRate(timerTask , invalidationPeriod, invalidationPeriod);
		logger.info("Mars Weather Service started");
	}
	
	/**
	 * Gets the last temperature data in Mars. 
	 * 
	 * @return an array containing one {@link SolData} object for each Sol. Theses objects encapsulate
	 *         the temperature measurements for the Sol.
	 * 
	 * @throws MarsWeatherServiceException if there is some problem acquiring the temperature data
	 * 
	 * @see SolData
	 */
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public SolData[] getTemperatures() throws MarsWeatherServiceException {
		logger.info("Average temperatures requested");
		/*
		 * Lock for protection against undesired cache invalidation between checking emptiness
		 * and effectively loading data from cache.
		 */
		readLock.lock();
		if (solDataMap.isEmpty()) {
			/*
			 * Cache is empty. NASA API access is required. Let's upgrade to write lock to make
			 * sure there are no readers in the critical section during cache update.
			 */
			readLock.unlock();
			writeLock.lock();
			try {
				// Second test is necessary to make sure another thread hasn't updated the cache before ourselves.
				if (solDataMap.isEmpty()) {
					logger.debug("Reading data from NASA server");
					loadDataFromServer();
				}
				/*
				 * Downgrade to read lock. We're done with cache update. However, we haven't finished
				 * data reading yet. Order is important here: Acquire read lock first, release write lock later.
				 */
				readLock.lock();
			}catch(Exception e){
				logger.error("Unexpected error during NASA's API access 0.", e);
			}finally {
				writeLock.unlock();
			}
		}else{
			logger.debug("Serving from local cache");
		}
		final SolData[] array = solDataMap.values().toArray(new SolData[0]);
		
		// Leaving critical section. Data is ready is to be returned to caller.
		readLock.unlock();
		
		return array;
	}
	
	/**
	 * Requests the last weather measurements from the NASA's API.
	 * 
	 * @throws MarsWeatherServiceException if some communication error occurred
	 */
	private void loadDataFromServer() throws MarsWeatherServiceException{
		logger.debug("Accessing NASA server");
		/*
		 * Fires HTTP request to NASA's API: 
		 */
		final Response response = nasaRequest.get();
		
		if (response.getStatus() == HTTP_STATUS_SUCCESS) {
			logger.debug("Successfull HTTP request");
			final String jsonString = response.readEntity(String.class);
			/*
			 * Transforming the JSON raw string into Java DTOs:
			 */
			final WeatherJsonParser parser = new WeatherJsonParser();
			try {
				final Map<Integer, WeatherDTO> dtoMap = parser.parseWeatherData(jsonString);
				/*
				 * Converting Java DTOs to model objects and storing them in cache:
				 */
				final ModelHelper modelHelper = new ModelHelper();
				solDataMap.putAll(modelHelper.convertDtoToModel(dtoMap));
				logger.debug("Local cache was updated");
			} catch (IOException e) {
				logger.error("JSON parsing failure", e);
				throw new MarsWeatherServiceException("Failure while processing NASA data.");
			}
		}else{
			logger.error("HTTP request failure");
			throw new MarsWeatherServiceException("NASA access failure. Status: "
					+response.getStatus()+" - "
					+response.getStatusInfo());
		}
	}
}
