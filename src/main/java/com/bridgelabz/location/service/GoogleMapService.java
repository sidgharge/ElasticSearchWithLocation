package com.bridgelabz.location.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.bridgelabz.location.model.LatLng;
import com.bridgelabz.location.model.LocationDetails;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface GoogleMapService {

	/**
	 * Measures the distance between two points and returns it in meters
	 * @param source LatLng object containing latitude and longitude of the source location
	 * @param destination LatLng object containing latitude and longitude of the destination location
	 * @return distance between source and destination in meters
	 */
	int getDistance(LatLng source, LatLng destination);
	
	/**
	 * Searches for the nearby housing complexes in given radius and returns as list
	 * @param currentLocation location containing latitude and longitude
	 * @param radius radius in meters in which housing complexes to be searched
	 * @return list of location details containing housing complex informations
	 */
	List<LocationDetails> getHousingComplexes(LatLng currentLocation,int radius);
	
	/**
	 * Searches for nearby location of a given location and returns as list
	 * @param currentLocation location for which nearby places to be searched
	 * @return list of location details having nearby locations information
	 */
	List<LocationDetails> getNearByPlaces(LatLng currentLocation);
	
	/**
	 * Gets details of the given location name and returns as a map
	 * @param searchString location name
	 * @return map of details of given location
	 */
	Map<String, String> getPlaceInfo(String searchString);
	
	/**
	 * Gets details of the given location and returns as a map
	 * @param lat latitude of the location
	 * @param lng longitude of the location
	 * @return map of details of given location
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	Map<String, String> getPlaceInfoFromLatLng(double lat, double lng) throws JsonProcessingException, IOException;
	
	/**
	 * Gets details of the given location and returns as a map
	 * @param lat latitude of the location
	 * @param lng longitude of the location
	 * @return map of sub-locality details of given location
	 */
	Map<String, String> getSublocalityDetails(double lat, double lng);
}
