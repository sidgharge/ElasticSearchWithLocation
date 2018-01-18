package com.bridgelabz.location.util;

import java.util.ArrayList;
import java.util.List;

import com.bridgelabz.location.model.LatLng;
import com.bridgelabz.location.model.LocationDetails;
import com.fasterxml.jackson.databind.JsonNode;

public class ResponseJsonUtil {

	public List<LocationDetails> getHousingComplex(JsonNode results) {
		List<LocationDetails> housingComplexes = new ArrayList<>();
		if (results != null) {
			for (int i = 0; i < results.size(); i++) {
				LocationDetails complex = new LocationDetails();

				double lat = results.get(i).get("geometry").get("location").get("lat").asDouble();
				double lng = results.get(i).get("geometry").get("location").get("lng").asDouble();

				LatLng latlng = new LatLng(lat, lng);
				complex.setName(results.get(i).get("name").asText());
				complex.setAddress(results.get(i).get("vicinity").asText());
				complex.setLocation(latlng);

				housingComplexes.add(complex);
			}
		}
		return housingComplexes;
	}

}
