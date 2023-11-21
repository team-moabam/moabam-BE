package com.moabam.api.application.auth.mapper;

import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.global.auth.model.PublicClaim;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthMapper {

	public static LoginResponse toLoginResponse(Member member, boolean isSignUp) {
		return LoginResponse.builder()
			.publicClaim(PublicClaim.builder()
				.id(member.getId())
				.nickname(member.getNickname())
				.role(member.getRole())
				.build())
			.isSignUp(isSignUp)
			.build();
	}

	public static TokenSaveValue toTokenSaveValue(String refreshToken, String ip) {
		return TokenSaveValue.builder()
			.refreshToken(refreshToken)
			.loginIp(ip)
			.build();
	}
}
