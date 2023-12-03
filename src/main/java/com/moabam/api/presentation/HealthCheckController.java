package com.moabam.api.presentation;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public String healthCheck() {
		return "Health Check Success";
	}

	@GetMapping("/serverTime")
	@ResponseStatus(HttpStatus.OK)
	public String serverTimeCheck() {
		return LocalDateTime.now().toString();
	}
}
