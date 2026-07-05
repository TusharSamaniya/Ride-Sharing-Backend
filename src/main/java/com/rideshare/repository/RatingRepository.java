package com.rideshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rideshare.entity.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long>{
	
	List<Rating> findByRatedUserId(Long userId);
	boolean existsByRideIdAndRaterId(Long rideId, Long raterId);
	
	@Query("SELECT AVG(r.stars) FROM Rating r WHERE r.ratedUser.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") Long userId);

}
