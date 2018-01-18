package com.bridgelabz.location.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.elastic.ElasticService;
import com.bridgelabz.location.model.HousingComplex;
import com.bridgelabz.location.model.LatLng;
import com.bridgelabz.location.model.Location;
import com.bridgelabz.location.model.LocationDetails;
import com.bridgelabz.location.model.Location;
import com.bridgelabz.location.repository.ComplexRepository;
import com.bridgelabz.location.repository.LocationRepository;
import com.bridgelabz.location.service.GoogleMapService;
import com.bridgelabz.location.service.LocationService;

@RestController
public class LocationController {

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	ElasticService elasticService;

	@Autowired
	ComplexRepository complexRepository;

	/*
	 * @Autowired Location2Repository location2Repository;
	 */

	@Autowired
	GoogleMapService service;
	
	@Autowired
	LocationService locationService;

	/**
	 * Adds all the housing complexes from database to elasticsearch index
	 */
	@RequestMapping("/addall")
	public ResponseEntity<Void> addFromDatabase() {
		try {
			locationService.addFromDatabase();
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Gets nearby places from given lat lng stored in elasticsearch index
	 * 
	 * @param lat latitude of the location
	 * @param lon longitude of the location
	 * @param distance in meters
	 * @return list of nearby locations stored in elasticsearch index
	 */
	@GetMapping("/nearby/{lat}/{lon}/{distance}")
	public ResponseEntity<List<Location>> getNearBy(@PathVariable double lat, @PathVariable double lon, @PathVariable String distance) {
		try {
			List<Location> nearbyLocations = locationService.getNearBy(lat, lon, distance);
			return new ResponseEntity<List<Location>>(nearbyLocations, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<List<Location>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/nearbycomplexes/{lat}/{lon}/{distance}")
	public ResponseEntity<List<HousingComplex>> getNearByComplexes(@PathVariable double lat, @PathVariable double lon, @PathVariable String distance) {
		try {
			List<HousingComplex> nearbyLocations = locationService.getNearByComplexes(lat, lon, distance);
			return new ResponseEntity<List<HousingComplex>>(nearbyLocations, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<List<HousingComplex>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	

	/**
	 * Iterates over the current locations and adds new nearby locations
	 * 
	 * @return number of new locations added to db and index
	 */
	@GetMapping("/addPlaces")
	public ResponseEntity<String> addPlaces() {
		try {
			String status = locationService.addPlaces();
			return new ResponseEntity<String>(status, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Iterate over the locations and adds nearby housing complexes to the location
	 */
	@GetMapping("/housingcomplex")
	public ResponseEntity<Void> addHousingComplexes() {
		try {
			locationService.addHousingComplexes();
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Iterate over the locations and adds nearby housing complexes to the location
	 * @param initialLatLng latitude and longitude of the location
	 * @return {@link ResponseEntity}
	 */
	@GetMapping("/circularhousingcomplex")
	public ResponseEntity<Void> circularSearchComplexes() {
		try {
			locationService.circularSearchComplexes();
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * updates area, zip and location of the locations
	 */
	@GetMapping("/update")
	public ResponseEntity<Void> updateLocations() {
		try {
			locationService.updateLocations();
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Returns housing complexes near the given location from elasticsearch index
	 * 
	 * @param locationId
	 *            id of the location
	 * @param page
	 *            page number
	 * @return list of housing complexes near the location id
	 */
	@GetMapping("/complex/{locationId}/{page}")
	public ResponseEntity<List<HousingComplex>> getHousingComplexes(@PathVariable String locationId, @PathVariable int page) {
		try {
			List<HousingComplex> complexes = locationService.getHousingComplexes(locationId, page);
			return new ResponseEntity<List<HousingComplex>>(complexes, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<List<HousingComplex>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Helps in mapping all lat lngs in the db to google map
	 * 
	 * @return list of lat lngs in the location db
	 */
	@CrossOrigin
	@RequestMapping("/latlng")
	public List<LatLng> getLatLngs() {
		return locationService.getLatLngs();
	}
	
	
	
	// @RequestMapping("/create")
		/**
		 * create new table and adds unique entries to the table from old table
		 */
		/*
		 * public void deleteDuplicates() { Iterable<Location> locations =
		 * locationRepository.findAll();
		 * 
		 * List<Location> list = new LinkedList<>(); locations.forEach(list::add);
		 * 
		 * List<Location2> list2 = new LinkedList<>();
		 * 
		 * for (int i = 0; i < list.size(); i++) { boolean add = true; for (int j = i +
		 * 1; j < list.size(); j++) { if (list.get(i).equals(list.get(j))) { add =
		 * false; break; } } if (add) { Location2 loc = new Location2();
		 * loc.copy(list.get(i)); list2.add(loc); } }
		 * 
		 * location2Repository.save(list2);
		 * 
		 * }
		 */

}
