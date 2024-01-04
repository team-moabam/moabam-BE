package com.moabam.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ClockHolder {

	LocalDateTime dateTime();

	LocalDate date();

	LocalTime time();
}
