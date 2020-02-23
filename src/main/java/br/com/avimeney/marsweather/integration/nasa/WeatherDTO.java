package br.com.avimeney.marsweather.integration.nasa;

public class WeatherDTO {

	public TemperatureDTO AT;
	public int solId;

	@Override
	public String toString() {
		return "WeatherData [AT=" + AT + "]";
	}
}
