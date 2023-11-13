package com.moabam.api.dto;

import java.util.List;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModifyRoomRequest(
	@NotBlank @Length(max = 20) String title,
	@Length(max = 100, message = "방 공지의 길이 100자 이하여야 합니다.") String announcement,
	@NotNull @Size(min = 1, max = 4) List<String> routines,
	@Pattern(regexp = "^(|\\d{4,8})$") String password,
	@Range(min = 0, max = 23) int certifyTime,
	@Range(min = 0, max = 10) int maxUserCount
) {

}
