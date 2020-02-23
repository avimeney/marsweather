package br.com.avimeney.marsweather.rest;

@SuppressWarnings("serial")
public class MarsWeatherServiceException extends Exception {

	public MarsWeatherServiceException(String msg) {
		super(msg, null, false, false);
	}
}
