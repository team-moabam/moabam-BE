package com.moabam.api.dto.auth;

import lombok.Builder;

@Builder
public record TokenSaveValue(
	String refreshToken,
	String loginIp
) {

}
