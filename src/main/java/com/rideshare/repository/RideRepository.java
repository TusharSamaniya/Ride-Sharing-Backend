package com.rideshare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rideshare.entity.Ride;
import com.rideshare.entity.RideStatus;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long>{
	
	List<Ride> findByRiderIdOrderByRequestedAtDesc(Long rideId);
	List<Ride> findByDriverIdOrderByRequestedAtDesc(Long driverId);
	Optional<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);
	List<Ride> findByStatus(RideStatus status);

}
