package com.moabam.api.dto;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.domain.entity.Member;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberMapper {

	public static Member toMember(Long socialId, String nickName) {
		return Member.builder()
			.socialId(socialId)
			.nickname(nickName)
			.bug(Bug.builder().build())
			.build();
	}

	public static LoginResponse toLoginResponse(Long memberId) {
		return LoginResponse.builder()
			.id(memberId)
			.build();
	}

	public static LoginResponse toLoginResponse(Long memberId, boolean isSignUp) {
		return LoginResponse.builder()
			.id(memberId)
			.isSignUp(isSignUp)
			.build();
	}
}
