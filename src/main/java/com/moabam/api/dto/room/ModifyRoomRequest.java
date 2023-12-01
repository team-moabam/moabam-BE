package com.moabam.api.dto.room;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ModifyRoomRequest(
	@NotBlank @Length(max = 20) String title,
	@Length(max = 100, message = "방 공지의 길이 100자 이하여야 합니다.") String announcement,
	@Pattern(regexp = "^(|\\d{4,8})$") String password,
	@Range(min = 0, max = 23) int certifyTime,
	@Range(min = 0, max = 10) int maxUserCount
) {

}
