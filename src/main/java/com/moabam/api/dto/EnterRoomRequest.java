package com.moabam.api.dto;

import jakarta.validation.constraints.Pattern;

public record EnterRoomRequest(
	@Pattern(regexp = "^(|[0-9]{4,8})$") String password
) {

}
