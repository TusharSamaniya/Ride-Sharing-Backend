package com.rideshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rideshare.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
	
	Optional<Vehicle> findByDriverId(Long driverId);
	boolean existsByLicensePlate(String licensePlate);

}
