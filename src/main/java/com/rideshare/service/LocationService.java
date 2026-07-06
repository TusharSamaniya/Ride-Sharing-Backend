package com.rideshare.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisGeoCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	// all driver locations stored under this redis key
	private static final String DRIVER_LOCATIONS_KEY = "driver.locations";

	// save driver locations to redis
	// called every time driver sends their gps update
	// redis command: feoadd driver: locations, longitude latitude
	public void updateDriverLocation(Long driverId, double latitude, double longitude) {
		redisTemplate.opsForGeo().add(DRIVER_LOCATIONS_KEY, new Point(longitude, latitude), "driver:" + driverId);

		log.info("updated location for driver {} -> lat: {} lng: {}", driverId, latitude, longitude);
	}

	public List<Long> findNearbyDriverIds(double latitude, double longitude, double radiusKm) {
// Create a circle: center point + radius
		Circle searchArea = new Circle(new Point(longitude, latitude),
				new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS));

// Search Redis for all drivers in this circle
		GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
				.radius(DRIVER_LOCATIONS_KEY, searchArea);

		List<Long> driverIds = new ArrayList<>();

		if (results != null) {
			results.forEach(result -> {
// Redis stores "driver:1" — we extract just "1"
				String memberName = result.getContent().getName();
				Long driverId = Long.parseLong(memberName.replace("driver:", ""));
				driverIds.add(driverId);
			});
		}

		log.info("Found {} drivers within {}km of ({},{})", driverIds.size(), radiusKm, latitude, longitude);

		return driverIds;
	}
	
	public void removeDriverLocation(Long driverId) {
		redisTemplate.opsForGeo().remove(DRIVER_LOCATIONS_KEY, "driver:" + driverId);
		log.info("Removed driver {} from Redis location store", driverId);
	}

}
