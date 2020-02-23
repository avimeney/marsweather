package br.com.avimeney.marsweather.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import br.com.avimeney.marsweather.integration.nasa.WeatherDTO;
import br.com.avimeney.marsweather.model.SolData;

/**
 * Helper class for converting DTO objects into domain model objects.
 * 
 * @author avimeney
 */

public class ModelHelper {

	/**
	 * Converts a map populated with DTO objects into a map populated with domain model objects.
	 * 
	 * @param dtoMap a map with Sol numbers as keys and DTO weather objects as values
	 * 
	 * @return a map with Sol numbers as keys and the corresponding weather model objects as values
	 */
	public Map<Integer, SolData> convertDtoToModel(Map<Integer, WeatherDTO> dtoMap) {
		
		final Map<Integer, SolData> modelMap = new HashMap<Integer, SolData>();
		
		dtoMap.forEach(new BiConsumer<Integer, WeatherDTO>() {
			@Override
			public void accept(Integer solId, WeatherDTO dto) {
				SolData solData = new SolData(solId, dto.AT.av, dto.AT.mn, dto.AT.mx);
				modelMap.put(solId, solData);
			}
		});
		return modelMap;
	}
}
