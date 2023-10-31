package com.moabam.api.dto;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ModifyRoomRequest(
	@NotBlank String title,
	@Pattern(regexp = "^(|[0-9]{4,8})$") String password,
	@Range(min = 0, max = 23) int certifyTime,
	@Range(min = 0, max = 10) int maxUserCount
) {

}
