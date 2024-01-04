package com.moabam.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod", "local"})
public class SystemClockHolder implements ClockHolder {

	@Override
	public LocalDateTime dateTime() {
		return LocalDateTime.now();
	}

	@Override
	public LocalDate date() {
		return LocalDate.now();
	}

	@Override
	public LocalTime time() {
		return LocalTime.now();
	}
}
