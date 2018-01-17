package com.bridgelabz.location.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bridgelabz.location.model.HousingComplex;

@Repository
public interface ComplexRepository extends CrudRepository<HousingComplex, Long>{

	
}
