package com.rideshare.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
	
	@Bean
	public NewTopic rideRequestTopic() {
		return TopicBuilder.name("ride.requested").build();
	}
	
	@Bean
	public NewTopic redeAcceptedtopic() {
		return TopicBuilder.name("ride.accepted").build();
	}
	
	@Bean
	public NewTopic rideCompletedtopic() {
		return TopicBuilder.name("ride.completed").build();
	}
	
	@Bean
	public NewTopic rideCancelledTopic() {
		return TopicBuilder.name("ride.cancelled").build();
	}

}
