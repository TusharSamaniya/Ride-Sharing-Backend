package com.rideshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rideshare.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
	Optional<User> findByPhoneNumber(String phoneNumber);
	boolean existsByEmail(String email);
	boolean existsByPhoneNumber(String phoneNumber);

}
