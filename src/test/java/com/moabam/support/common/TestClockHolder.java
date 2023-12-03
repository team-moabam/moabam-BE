package com.moabam.support.common;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.moabam.global.common.util.ClockHolder;

@Component
@Profile("test")
public class TestClockHolder implements ClockHolder {

	@Override
	public LocalDateTime times() {
		return LocalDateTime.of(2023, 12, 3, 14, 30, 0);
	}

	@Override
	public LocalDate date() {
		return LocalDateTime.now().toLocalDate();
	}
}
