package com.moabam.support.fixture;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.member.Member;

public final class MemberFixture {

	public static final String SOCIAL_ID = "1";
	public static final String NICKNAME = "모아밤";

	public static Member member() {
		return Member.builder()
			.socialId(SOCIAL_ID)
			.bug(BugFixture.bug())
			.build();
	}

	public static Member member(Bug bug) {
		return Member.builder()
			.socialId(SOCIAL_ID)
			.bug(bug)
			.build();
	}

	public static Member member(String socialId, String nickname) {
		return Member.builder()
			.socialId(socialId)
			.bug(BugFixture.bug())
			.build();
	}
}
