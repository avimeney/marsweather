package br.com.avimeney.marsweather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Temperature data for a specific Sol. 
 *  
 * @author avimeney
 */

public class SolData {
	
	/*
	 * Field names are annotated to guarantee compact property names in the generated JSON
	 */
	
	@JsonProperty("id")
	private int solId;
	
	@JsonProperty("avg")
	private float averagetemperature;
	
	@JsonProperty("min")
	private float minimumTemperature;
	
	@JsonProperty("max")
	private float maximumTemperature;
	
	public SolData() {
	}
	
	public SolData(int solId, float averagetemperature, float minimumTemperature, float maximumTemperature) {
		super();
		this.solId = solId;
		this.averagetemperature = averagetemperature;
		this.minimumTemperature = minimumTemperature;
		this.maximumTemperature = maximumTemperature;
	}

	public int getSolId() {
		return solId;
	}

	public float getAveragetemperature() {
		return averagetemperature;
	}
	
	public float getMinimumTemperature() {
		return minimumTemperature;
	}
	
	public float getMaximumTemperature() {
		return maximumTemperature;
	}
}
