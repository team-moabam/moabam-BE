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
}
