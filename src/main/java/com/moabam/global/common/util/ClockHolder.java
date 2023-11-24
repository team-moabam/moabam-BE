package com.moabam.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClockHolder {

	LocalDateTime times();

	LocalDate date();
}
