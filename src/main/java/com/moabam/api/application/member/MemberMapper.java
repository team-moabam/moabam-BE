package com.moabam.api.application.member;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.member.Member;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberMapper {

	public static Member toMember(Long socialId) {
		return Member.builder()
			.socialId(String.valueOf(socialId))
			.bug(Bug.builder().build())
			.build();
	}
}
