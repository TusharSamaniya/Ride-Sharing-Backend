package com.rideshare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rideshare.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
	
	Optional<Driver> findByUserId(Long userId);
	boolean existsByUserId(Long userId);
	List<Driver> findByIsAvailableTrueAndIsVerifiedTrue();
	Optional<Driver> findByLicenseNumber(String licenseNumber);

}
