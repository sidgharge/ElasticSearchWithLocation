package com.bridgelabz.location.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table
public class HousingComplex {

	@Id
	@GeneratedValue(generator = "complex", strategy = GenerationType.AUTO)
	@GenericGenerator(name = "complex", strategy = "native")
	private long complexId;

	private long locationId;

	private String complexName;

	@Embedded
	private LatLng latLng;

	public long getComplexId() {
		return complexId;
	}

	public void setComplexId(long complexId) {
		this.complexId = complexId;
	}

	public long getLocationId() {
		return locationId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public String getComplexName() {
		return complexName;
	}

	public void setComplexName(String complexName) {
		this.complexName = complexName;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}
	
	@Override
	public boolean equals(Object obj) {
		HousingComplex complex = (HousingComplex) obj;
		if (complex.getComplexId() == this.getComplexId()) {
			return true;
		}
		return false;
	}

}
