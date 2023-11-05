package com.moabam.support.fixture;

import com.moabam.api.domain.entity.Member;

public final class MemberFixture {

	public static final String SOCIAL_ID = "test123";
	public static final String NICKNAME = "모아밤";
	public static final String PROFILE_IMAGE = "/profile/moabam.png";

	public static Member member() {
		return Member.builder()
			.socialId(SOCIAL_ID)
			.nickname(NICKNAME)
			.profileImage(PROFILE_IMAGE)
			.bug(BugFixture.bug())
			.build();
	}
}
