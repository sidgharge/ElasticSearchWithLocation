package com.bridgelabz.location.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bridgelabz.location.model.Location;


@Repository
public interface LocationRepository extends CrudRepository<Location, Long>{

}
