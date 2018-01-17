package com.bridgelabz.location.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LatLng {

	private double lat;

	@Column(name = "lng")
	private double lon;

	public LatLng(double lat, double lng) {
		this.lat = lat;
		this.lon = lng;
	}

	public LatLng() {

	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

}
