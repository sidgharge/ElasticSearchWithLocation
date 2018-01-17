package com.bridgelabz.location.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bridgelabz.location.model.LatLng;
import com.bridgelabz.location.model.LocationDetails;
import com.bridgelabz.location.util.ResponseJsonUtil;
import com.bridgelabz.location.util.RestCallUtililty;
import com.bridgelabz.location.util.Utility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GoogleMapServiceImpl implements GoogleMapService {

	@Value("${googlemap.key}")
	String key;

	@Autowired
	Utility utility;
	
	@Autowired
	RestCallUtililty restCallUtil;

	/**
	 * @param source
	 * @param destination
	 * @return
	 */
	public int getDistance(LatLng source, LatLng destination) {

		String origin = source.getLat() + "," + source.getLon();
		String destinations = destination.getLat() + "," + destination.getLon();

		String mapApiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?" + "origins=" + origin
				+ "&destinations=" + destinations + "&key=" + key;

		ResteasyClient restCall = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = restCall.target(mapApiUrl);

		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

		String responseString = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode responseJson = null;
		try {
			responseJson = mapper.readTree(responseString);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int distanceInMeters = responseJson.get("rows").get(0).get("elements").get(0).get("distance").get("value")
				.asInt();

		return distanceInMeters;
	}

	/**
	 * @param currentLocation
	 * @return
	 * @throws InterruptedException
	 */
/*	public List<LocationDetails> getHousingComplexes(LatLng currentLocation) throws InterruptedException {

		String location = currentLocation.getLat() + "," + currentLocation.getLon();

		String mapApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location
				+ "&radius=1000&keyword=Apartment|CHS&strictbounds&key=" + key;

		List<LocationDetails> housingComplexes = new ArrayList<>();

		ResteasyClient restCall = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = restCall.target(mapApiUrl);

		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

		String responseString = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode responseJson = null;
		try {
			responseJson = mapper.readTree(responseString);
			JsonNode results = null;
			results=responseJson.get("results");
			JsonNode nextPageResults = null;
			
			if (responseJson.get("next_page_token") != null) {
				Thread.sleep(3000);
				String nextPageToken = responseJson.get("next_page_token").asText();
				String nextPageUrl = mapApiUrl + "&pagetoken=" + nextPageToken;
				System.out.println(nextPageUrl);

				target = restCall.target(nextPageUrl);
				response = target.request().accept(MediaType.APPLICATION_JSON).get();

				String newPageResponse = response.readEntity(String.class);
				//System.out.println(newPageResponse);
				JsonNode nextPageResponse = mapper.readTree(newPageResponse);
				nextPageResults = nextPageResponse.get("results");
				
			
				if (nextPageResponse.get("next_page_token") != null) {
					nextPageToken=nextPageResponse.get("next_page_token").asText();
					nextPageUrl = mapApiUrl + "&pagetoken=" + nextPageToken;
					Thread.sleep(3000);
					target = restCall.target(nextPageUrl);
					response = target.request().accept(MediaType.APPLICATION_JSON).get();

					newPageResponse = response.readEntity(String.class);
					//System.out.println(newPageResponse);
					nextPageResponse = mapper.readTree(newPageResponse);
					results=nextPageResponse.get("results");
					System.out.println("========>"+results.size());
				}
		
			if(results!=null) {
				for (int i = 0; i < results.size(); i++) {
					LocationDetails complex = new LocationDetails();
	
					double lat = results.get(i).get("geometry").get("location").get("lat").asDouble();
					double lng = results.get(i).get("geometry").get("location").get("lng").asDouble();
	
					LatLng latlng = new LatLng(lat, lng);
					complex.setName(results.get(i).get("name").asText());
					complex.setAddress(results.get(i).get("vicinity").asText());
					complex.setLocation(latlng);
					//System.out.println(i);
					housingComplexes.add(complex);
	
					if ((results.size() - 1) == i && nextPageResults != null) {
						i = 0;
						results = nextPageResults;
						nextPageResults = null;
					}
			   }
			}
		} 
		}catch (IOException e) {
			e.printStackTrace();
		}

		restCall.close();
		return housingComplexes;
	}*/
	
	public List<LocationDetails> getHousingComplexes(LatLng currentLocation,int radius) {

		String location = currentLocation.getLat() + "," + currentLocation.getLon();

		String mapApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location
				+ "&radius="+radius+"&keyword=Apartment|Society|CHS|Complex&strictbounds&key=" + key;


		List<LocationDetails> housingComplexes = new ArrayList<>();
		
		ResponseJsonUtil responseUtil=new ResponseJsonUtil();
		int counter=0;
		try {
			
			JsonNode responseJson = restCallUtil.getResponse(mapApiUrl);

			JsonNode results = responseJson.get("results");
			housingComplexes.addAll(responseUtil.getHousingComplex(results));
			
			JsonNode nextPageResults = null;
			if(responseJson.get("next_page_token")!=null) {
				String nextPageToken=responseJson.get("next_page_token").asText();
				while(nextPageToken!=null) {
					counter++;
					 String nextPageUrl=mapApiUrl+"&pagetoken="+nextPageToken;
					 Thread.sleep(4000);
					 JsonNode nextPageResponse = restCallUtil.getResponse(nextPageUrl);
					 nextPageResults=nextPageResponse.get("results");
					 housingComplexes.addAll(responseUtil.getHousingComplex(nextPageResults));
					 if(nextPageResponse.get("next_page_token")!=null) {
						 nextPageResults=nextPageResponse.get("results");
						 nextPageToken=nextPageResponse.get("next_page_token").asText();
					 }else {
						 nextPageToken=null;
					 }
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(housingComplexes.size()+" "+counter);
		return housingComplexes;
	}


	/**
	 * @param currentLocation
	 * @return
	 */
	public List<LocationDetails> getNearByPlaces(LatLng currentLocation) {

		String location = currentLocation.getLat() + "," + currentLocation.getLon();

		List<LocationDetails> nearByPlaces = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();

		ResteasyClient restCall = new ResteasyClientBuilder().build();

		for (int radius = 350; radius <= 3350; radius += 1000) {

			String mapApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + location
					+ "&radius=" + radius + "&types=sublocality_level_2&key=" + key;

			ResteasyWebTarget target = restCall.target(mapApiUrl);

			Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

			String responseString = response.readEntity(String.class);
			
			//System.out.println(responseString);

			JsonNode responseJson = null;

			try {
				responseJson = mapper.readTree(responseString);
			} catch (IOException e) {
				e.printStackTrace();
			}

			JsonNode results = responseJson.get("results");

			for (int i = 0; i < results.size(); i++) {
				int flag = 0;

				double lat = results.get(i).get("geometry").get("location").get("lat").asDouble();
				double lng = results.get(i).get("geometry").get("location").get("lng").asDouble();

				LatLng latlng = new LatLng(lat, lng);
				LocationDetails place = new LocationDetails();
				place.setName(results.get(i).get("name").asText());
				place.setAddress(results.get(i).get("vicinity").asText());
				place.setLocation(latlng);

				for (int j = 0; j < nearByPlaces.size(); j++) {
					if (nearByPlaces.get(j).getName().equals(place.getName())) {
						flag = 1;
					}
				}

				if (flag == 0) {
					nearByPlaces.add(place);
				}
			}

		}
		restCall.close();
		return nearByPlaces;
	}

	public Map<String, String> getPlaceInfo(String searchString) {

		Map<String, String> placeInfo = new HashMap<>();

		ResteasyClient restCall = new ResteasyClientBuilder().build();

		String[] words = searchString.split(" ");
		searchString = "";

		int i = 0;
		while (i < words.length) {
			searchString = words[i] + "+" + searchString;
			i++;
		}
		String mapApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + searchString + "&key=" + key;

		ResteasyWebTarget target = restCall.target(mapApiUrl);

		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

		String responseString = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode responseJson = null;

		try {
			responseJson = mapper.readTree(responseString);

			JsonNode results = responseJson.get("results");

			double lat = results.get(0).get("geometry").get("location").get("lat").asDouble();
			double lng = results.get(0).get("geometry").get("location").get("lng").asDouble();

			String location = lat + "," + lng;

			String geoCodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?&types=postal_code&latlng="
					+ location + "&keyword=" + words[0] + "&rankBy=keyword&key=" + key;

			target = restCall.target(geoCodeUrl);

			response = target.request().accept(MediaType.APPLICATION_JSON).get();

			responseString = response.readEntity(String.class);

			responseJson = mapper.readTree(responseString);
			
			int flag = 0;
			for (int k = 0; k <= responseJson.get("results").size(); k++) {

				results = responseJson.get("results").get(k);

				for (int index = 0; index < results.get("types").size(); index++) {
					if (results.get("types").get(index).asText().equals("postal_code")) {
						flag = 1;
						break;
					}
				}
				if (flag == 1) {
					break;
				}
			}
			for (int j = 0; j < results.size(); j++) {

				JsonNode types = results.get("address_components");
				
				for (int index = 0; index < types.get(j).get("types").size(); index++) {
					JsonNode record=types.get(j).get("types").get(index);
					if (types.get(j).get("types").get(index).asText().equals("postal_code")) {
						placeInfo.put("zipcode", types.get(j).get("long_name").asText());
						
					} else if (types.get(j).get("types").get(index).asText().equals("sublocality_level_1")) {
						placeInfo.put("sublocality_level_1", types.get(j).get("long_name").asText());
						
					} else if (types.get(j).get("types").get(index).asText().equals("locality")) {
						placeInfo.put("locality", types.get(j).get("long_name").asText());
						
					} else if (types.get(j).get("types").get(index).asText().equals("country")) {
						placeInfo.put("country", types.get(j).get("long_name").asText());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return placeInfo;

	}

	public Map<String, String> getPlaceInfoFromLatLng(double lat, double lng) throws JsonProcessingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		
		ResteasyClient restCall = new ResteasyClientBuilder().build();
		
		Map<String, String> placeInfo = new HashMap<>();

		String geoCodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?&components=postal_code&latlng=" + lat + "," + lng
				+ "&keyword=" + "&rankBy=keyword&key=" + key;
		
		JsonNode responseJson = null;
		
		ResteasyWebTarget target = restCall.target(geoCodeUrl);

		Response response = target.request().accept(MediaType.APPLICATION_JSON).get();

		String responseString = response.readEntity(String.class);

		target = restCall.target(geoCodeUrl);

		response = target.request().accept(MediaType.APPLICATION_JSON).get();

		responseString = response.readEntity(String.class);

		responseJson = mapper.readTree(responseString);
	
		JsonNode results = responseJson.get("results");
		System.out.println(results.toString());
		int flag = 0;
		for (int k = 0; k < responseJson.get("results").size(); k++) {

			results = responseJson.get("results").get(k);

			for (int index = 0; index < results.get("types").size(); index++) {
				if (results.get("types").get(index).asText().equals("postal_code")) {
					flag = 1;
					break;
				}
			}
			if (flag == 1) {
				break;
			}
		}
		JsonNode types = results.get("address_components");
		for (int j = 0; j < types.size(); j++) {
			
			
			
	
			for (int index = 0; index < types.get(j).get("types").size()&&j<types.size(); index++) {
				
				//JsonNode record=types.get(j).get("types").get(index);
				if (types.get(j).get("types").get(index).asText().equals("postal_code")) {
					placeInfo.put("zipcode", types.get(j).get("long_name").asText());
					
				} else if (types.get(j).get("types").get(index).asText().equals("sublocality_level_1")) {
					placeInfo.put("sublocality_level_1", types.get(j).get("long_name").asText());
					
				} else if (types.get(j).get("types").get(index).asText().equals("locality")) {
					placeInfo.put("locality", types.get(j).get("long_name").asText());
					
				} else if (types.get(j).get("types").get(index).asText().equals("country")) {
					placeInfo.put("country", types.get(j).get("long_name").asText());
				}
				
			}
			
		}
	return placeInfo;

	}
	
	public Map<String, String> getSublocalityDetails(double lat, double lng) {

		Map<String, String> placeInfo = new HashMap<>();

		String location = lat + "," + lng;
		
		String geoCodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?&types=postal_code&latlng="
				+ location + "+&rankBy=keyword&key=" + key;

		JsonNode responseJson = null;

		try {
			responseJson = restCallUtil.getResponse(geoCodeUrl);

			JsonNode results = responseJson.get("results");
		
			for (int k = 0; k < results.size(); k++) {
				JsonNode record = results.get(k).get("address_components");
				for (int j = 0; j < record.size(); j++) {

					for (int index = 0; index < record.get(j).get("types").size(); index++) {
						String type = record.get(j).get("types").get(index).asText();
						if (type.equals("postal_code")) {
							placeInfo.put("zipcode", record.get(j).get("long_name").asText());

						} else if (type.equals("sublocality_level_1")) {
							placeInfo.put("sublocality_level_1", record.get(j).get("long_name").asText());

						} else if (type.equals("locality")) {
							placeInfo.put("locality", record.get(j).get("long_name").asText());

						} else if (type.equals("country")) {
							placeInfo.put("country", record.get(j).get("long_name").asText());
						}else if(type.equals("sublocality_level_2")) {
							placeInfo.put("Level2", record.get(j).get("long_name").asText());
						}
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return placeInfo;

	}

}
