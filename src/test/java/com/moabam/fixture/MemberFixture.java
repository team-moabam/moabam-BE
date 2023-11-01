package com.moabam.fixture;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.domain.entity.Member;

public final class MemberFixture {

	public static final String SOCIAL_ID = "test123";
	public static final String NICKNAME = "모아밤";
	public static final String PROFILE_IMAGE = "/profile/moabam.png";
	public static final int MORNING_BUG = 10;
	public static final int NIGHT_BUG = 20;
	public static final int GOLDEN_BUG = 30;

	public static Member member() {
		Bug bug = Bug.builder()
			.morningBug(MORNING_BUG)
			.nightBug(NIGHT_BUG)
			.goldenBug(GOLDEN_BUG)
			.build();

		return Member.builder()
			.socialId(SOCIAL_ID)
			.nickname(NICKNAME)
			.profileImage(PROFILE_IMAGE)
			.bug(bug)
			.build();
	}
}
