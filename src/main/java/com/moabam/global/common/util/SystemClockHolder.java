package com.moabam.global.common.util;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class SystemClockHolder implements ClockHolder {

	@Override
	public LocalDateTime times() {
		return LocalDateTime.now();
	}
}
