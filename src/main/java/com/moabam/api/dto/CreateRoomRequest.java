package com.moabam.api.dto;

import java.util.List;

import org.hibernate.validator.constraints.Range;

import com.moabam.api.domain.entity.enums.RoomType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
	@NotBlank String title,
	@Pattern(regexp = "^(|[0-9]{4,8})$") String password,
	@NotNull @Size(min = 0, max = 4) List<String> routines,
	@NotNull RoomType roomType,
	@Range(min = 0, max = 23) int certifyTime,
	@Range(min = 0, max = 10) int maxUserCount
) {

}
