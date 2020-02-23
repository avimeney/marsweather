package br.com.avimeney.marsweather.integration.nasa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parser responsible for transforming NASA's JSON into Java DTO objects.
 * This parser is based on version 1.0 of NASA InSight REST API.
 * 
 * @author avimeney
 * 
 * @see WeatherDTO
 */

public class WeatherJsonParser {

	/**
	 * Transforms NASA's JSON string from NASA InSight REST API into Java DTO objects.
	 * 
	 * @param jsonString JSON String from NASA InSight REST API version 1.0
	 * 
	 * @return map where the Sol number is used as key to the corresponding weather data 
	 * 
	 * @throws IOException if the parsing fails
	 */
	public Map<Integer, WeatherDTO> parseWeatherData(final String jsonString) throws IOException {
		/*
		 * We don't need all JSON attributes from NASA. We'll set FAIL_ON_UNKNOWN_PROPERTIES
		 * to false so not mapped attributes won't break parsing.
		 */
		final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// Sol number as key and Sol weather data as value
		final Map<Integer, WeatherDTO> weatherMap = new HashMap<Integer, WeatherDTO>();
		/*
		 * The first level of NASA's JSON contains Sol numbers as well as other different properties acting as JSON keys.
		 * To deal with this heterogeneous modeling, we decided to use a straightforward String to JsonNode mapping, at
		 * least, at this first level: 
		 */
		final Map<String, JsonNode> nasaData = mapper.readValue(jsonString, new TypeReference<Map<String, JsonNode>>(){});
		/*
		 * An useful property from the NASA's JSON is the first level sol_keys, that consists in an array of Sol numbers.
		 * Let's transform this JSON property into a Java ArrayList:
		 */
		final String solKeysJson = nasaData.get("sol_keys").toString();
		final ArrayList<String> solKeys = mapper.readValue(solKeysJson, new TypeReference<ArrayList<String>>(){});
		/*
		 * Based on this Sol numbers (keys) array, we'll get the corresponding JSON string and then parse the
		 * weather data for each Sol. 
		 */
		for (String solKey:solKeys) {
			// JSON string for the Sol
			final JsonNode jsonSolData = nasaData.get(solKey);
			// Parsing JSON for the Sol
			final WeatherDTO weatherData = mapper.readValue(jsonSolData.toString(), WeatherDTO.class);
			// Storing the parsed weather data for the Sol
			weatherMap.put(Integer.parseInt(solKey), weatherData);
		}
		
		return weatherMap;
	}
}
