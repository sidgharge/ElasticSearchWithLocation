package com.bridgelabz.location.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.bridgelabz.elastic.ElasticService;
import com.bridgelabz.location.model.HousingComplex;
import com.bridgelabz.location.model.LatLng;
import com.bridgelabz.location.model.Location;
import com.bridgelabz.location.model.LocationDetails;
import com.bridgelabz.location.repository.ComplexRepository;
import com.bridgelabz.location.repository.LocationRepository;

@Service
public class LocationService {

	@Autowired
	ElasticService elasticService;

	@Autowired
	ComplexRepository complexRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	GoogleMapService service;

	/**
	 * Creates a bulk request and Adds all the housing complexes from database to
	 * elasticsearch index
	 * 
	 * @throws IOException
	 */
	public void addFromDatabase() throws IOException {
		Map<String, HousingComplex> locationMap = new HashMap<>();

		Iterable<HousingComplex> locations = complexRepository.findAll();
		int count = 0;
		for (HousingComplex location : locations) {
			locationMap.put(String.valueOf(location.getComplexId()), location);
			count++;
			if (count % 10 == 0) {
				System.out.println("Done: " + count);
			}
		}

		System.out.println("Size: " + locationMap.size());
		elasticService.bulkRequest("complex", "complex", HousingComplex.class, locationMap);

	}

	/**
	 * Gets nearby places from given lat lng stored in elasticsearch index
	 * 
	 * @param lat
	 *            latitude of the location
	 * @param lon
	 *            longitude of the location
	 * @param distance
	 *            in meters
	 * @return list of nearby locations stored in elasticsearch index
	 * @throws IOException
	 */
	public List<Location> getNearBy(@PathVariable double lat, @PathVariable double lon, @PathVariable String distance)
			throws IOException {
		List<Location> locations = elasticService.getNearByLocations("loc", "loc", Location.class, lat, lon, distance);
		return locations;

	}

	/**
	 * Iterates over the current locations and adds new nearby locations
	 * 
	 * @return number of new locations added to db and index
	 */
	public String addPlaces() throws IOException {
		Iterable<Location> locations = locationRepository.findAll();
		int counter = 0;
		int addedlocations = 0;
		for (Location location : locations) {
			/*
			 * if (counter < 48) { counter++; continue; }
			 */
			/*
			 * if (counter >= 10) { break; }
			 */

			counter++;
			List<LocationDetails> details = service.getNearByPlaces(location.getLatLng());
			System.out.println("Got details from map...");
			for (LocationDetails locationDetails : details) {
				List<Location> nearByLocations = elasticService.getNearByLocations("loc", "loc", Location.class,
						locationDetails.getLocation().getLat(), locationDetails.getLocation().getLon(), "1000");
				System.out.println("Nearby location count: " + nearByLocations.size());
				if (nearByLocations == null || nearByLocations.isEmpty()) {
					Location newLocation = new Location();
					newLocation.copyFromLocationDetails(location, locationDetails);
					newLocation = locationRepository.save(newLocation);
					elasticService.save(newLocation, "loc", "loc", String.valueOf(newLocation.getLocationId()));
					addedlocations++;
				}
			}
			System.out.println("Done: " + counter);
		}
		return "Got new location count: " + addedlocations;
	}

	/**
	 * Iterate over the locations and adds nearby housing complexes to the location
	 * 
	 * @throws IOException
	 */
	public void addHousingComplexes() throws IOException {
		Iterable<Location> locations = locationRepository.findAll();
		int counter = 0;
		int added = 0;
		for (Location location : locations) {
			/*
			 * if (counter < 50) { counter++; continue; } if (counter > 121) { break; }
			 */
			counter++;
			List<LocationDetails> complexes = service.getHousingComplexes(location.getLatLng(), 1000);
			for (LocationDetails locationDetails : complexes) {
				// List<HousingComplex> results = elasticUtility.searchByTermAndValue("complex",
				// "complex", HousingComplex.class, "latLng", locationDetails.getLocation(), 0);
				List<HousingComplex> results = elasticService.getNearByLocations("complex", "complex",
						HousingComplex.class, locationDetails.getLocation().getLat(),
						locationDetails.getLocation().getLon(), "5");
				if (results.size() > 0) {
					continue;
				}
				added++;
				HousingComplex complex = new HousingComplex();
				complex.setLocationId(location.getLocationId());
				complex.setComplexName(locationDetails.getName());
				complex.setLatLng(locationDetails.getLocation());
				complex = complexRepository.save(complex);
				elasticService.save(complex, "complex", "complex", String.valueOf(complex.getComplexId()));
			}

			System.out.println("Done: " + counter + ", Added: " + added);
		}
	}

	/**
	 * Iterate over the locations and adds nearby housing complexes to the location
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void circularSearchComplexes() throws IOException, InterruptedException {
		Iterable<Location> locations = locationRepository.findAll();
		int counter = 0;
		int added = 0;
		for (Location location : locations) {
			/*
			 * if (counter < 11) { counter++; continue; } if (counter > 121) { break; }
			 */
			counter++;
			List<LocationDetails> complexes = service.getHousingComplexes(location.getLatLng(), 500);
			for (LocationDetails locationDetails : complexes) {
				// List<HousingComplex> results = elasticUtility.searchByTermAndValue("complex",
				// "complex", HousingComplex.class, "latLng", locationDetails.getLocation(), 0);
				List<HousingComplex> results = elasticService.getNearByLocations("complex", "complex",
						HousingComplex.class, locationDetails.getLocation().getLat(),
						locationDetails.getLocation().getLon(), "5");
				if (results.size() > 0) {
					continue;
				}
				added++;
				HousingComplex complex = new HousingComplex();
				complex.setLocationId(location.getLocationId());
				complex.setComplexName(locationDetails.getName());
				complex.setLatLng(locationDetails.getLocation());
				complex = complexRepository.save(complex);
				elasticService.save(complex, "complex", "complex", String.valueOf(complex.getComplexId()));
			}
			Thread.sleep(1000);
			List<HousingComplex> complex500 = elasticService.getNearByLocations("complex", "complex",
					HousingComplex.class, location.getLatLng().getLat(), location.getLatLng().getLon(), "500");
			List<HousingComplex> complex400 = elasticService.getNearByLocations("complex", "complex",
					HousingComplex.class, location.getLatLng().getLat(), location.getLatLng().getLon(), "400");
			complex500.removeAll(complex400);

			for (HousingComplex housingComplex : complex500) {
				List<LocationDetails> complexes2 = service.getHousingComplexes(housingComplex.getLatLng(), 600);
				for (LocationDetails locationDetails : complexes2) {
					// List<HousingComplex> results = elasticUtility.searchByTermAndValue("complex",
					// "complex", HousingComplex.class, "latLng", locationDetails.getLocation(), 0);
					List<HousingComplex> results = elasticService.getNearByLocations("complex", "complex",
							HousingComplex.class, locationDetails.getLocation().getLat(),
							locationDetails.getLocation().getLon(), "5");
					if (results.size() > 0) {
						continue;
					}
					added++;
					HousingComplex complex = new HousingComplex();
					complex.setLocationId(location.getLocationId());
					complex.setComplexName(locationDetails.getName());
					complex.setLatLng(locationDetails.getLocation());
					complex = complexRepository.save(complex);
					elasticService.save(complex, "complex", "complex", String.valueOf(complex.getComplexId()));
				}
			}

			System.out.println("Done: " + counter + ", Added: " + added);
		}
	}

	/**
	 * Returns housing complexes near the given location
	 * 
	 * @param locationId
	 *            id of the location
	 * @param page
	 *            page number
	 * @return list of housing complexes near the location id
	 * @throws IOException
	 */
	public void updateLocations() throws IOException {
		Iterable<Location> locations = locationRepository.findAll();
		int counter = 0;
		int changedPos = 0;
		for (Location location : locations) {
			counter++;
			if (counter % 10 == 0) {
				System.out.println(counter);
			}
			/*
			 * Map<String, String> locationInfo =
			 * service.getPlaceInfoFromLatLng(location.getLatLng().getLat(),
			 * location.getLatLng().getLon());
			 */

			if (/* counter < 10 && counter > 0 */ true) {
				Map<String, String> locationInfo = service.getSublocalityDetails(location.getLatLng().getLat(),
						location.getLatLng().getLon());
				boolean isChanged = false;
				if (locationInfo.get("zipcode") != null
						&& !locationInfo.get("zipcode").equals(String.valueOf(location.getZip()))) {

					location.setZip(Integer.parseInt(locationInfo.get("zipcode")));
					isChanged = true;
				}
				if (locationInfo.get("sublocality_level_1") != null
						&& !locationInfo.get("sublocality_level_1").equals(location.getArea())) {
					location.setArea(locationInfo.get("sublocality_level_1"));
					isChanged = true;
				}
				if (locationInfo.get("Level2") != null && !locationInfo.get("Level2").equals(location.getPlace())) {
					location.setPlace(locationInfo.get("Level2"));
					isChanged = true;
				}
				if (isChanged) {
					changedPos++;
					locationRepository.save(location);
					elasticService.save(location, "loc", "loc", String.valueOf(location.getLocationId()));

				}

			}

		}
		System.out.println("Changed count: " + changedPos);
	}
	
	/**
	**
	 * Returns housing complexes near the given location from elasticsearch index
	 * 
	 * @param locationId
	 *            id of the location
	 * @param page
	 *            page number
	 * @return list of housing complexes near the location id
	 * @throws IOException
	 */
	public List<HousingComplex> getHousingComplexes(@PathVariable String locationId, @PathVariable int page) throws IOException {
		int from = 20 * (page - 1);
		return elasticService.searchByTermAndValue("complex", "complex", HousingComplex.class, "locationId",
				locationId, from);
	}
	
	
	/**
	 * Helps in mapping all lat lngs in the db to google map
	 * 
	 * @return list of lat lngs in the location db
	 */
	public List<LatLng> getLatLngs() {
		Iterable<Location> locations = locationRepository.findAll();
		List<LatLng> latLngs = new ArrayList<>();
		for (Location location : locations) {
			latLngs.add(location.getLatLng());
		}
		return latLngs;
	}
}
