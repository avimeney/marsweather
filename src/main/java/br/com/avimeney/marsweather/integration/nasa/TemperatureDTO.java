package br.com.avimeney.marsweather.integration.nasa;

public class TemperatureDTO {
	public float av;
	public int ct;
	public float mn;
	public float mx;
	
	@Override
	public String toString() {
		return "TemperatureData [av=" + av + ", ct=" + ct + ", mn=" + mn + ", mx=" + mx + "]";
	}
}
