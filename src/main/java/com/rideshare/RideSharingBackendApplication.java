package com.rideshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RideSharingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RideSharingBackendApplication.class, args);
	}

}
