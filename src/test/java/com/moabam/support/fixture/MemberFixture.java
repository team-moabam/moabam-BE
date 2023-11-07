package com.moabam.support.fixture;

import com.moabam.api.domain.entity.Member;

public final class MemberFixture {

	public static final long SOCIAL_ID = 1L;
	public static final String NICKNAME = "모아밤";

	public static Member member() {
		return Member.builder()
			.socialId(SOCIAL_ID)
			.nickname(NICKNAME)
			.bug(BugFixture.bug())
			.build();
	}

	public static Member member(Long socialId, String nickname) {
		return Member.builder()
			.socialId(socialId)
			.nickname(nickname)
			.bug(BugFixture.bug())
			.build();
	}
}
