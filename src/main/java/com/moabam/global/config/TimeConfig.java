package com.moabam.global.config;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

	private Clock clock;

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

	public void main(String[] args) {
		LocalDateTime.now(clock);
	}
}
