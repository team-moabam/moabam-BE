package com.moabam.support.fixture;

import java.util.Set;

import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.dto.member.MemberInfoSearchResponse;

public class MemberInfoSearchFixture {

	private static final String NICKNAME = "nickname";
	private static final String PROFILE_IMAGE = "profileuri";
	private static final String INTRO = "intro";
	private static final long TOTAL_CERTIFY_COUNT = 15;
	private static final Set<BadgeType> BADGE_TYPES = Set.of(BadgeType.MORNING_BIRTH, BadgeType.NIGHT_BIRTH);

	public static MemberInfoSearchResponse friendMemberInfo() {
		return friendMemberInfo(TOTAL_CERTIFY_COUNT);
	}

	public static MemberInfoSearchResponse friendMemberInfo(long total) {
		return new MemberInfoSearchResponse(NICKNAME, PROFILE_IMAGE, INTRO, total, BADGE_TYPES);
	}

	public static MemberInfoSearchResponse myInfo() {
		return new MemberInfoSearchResponse(NICKNAME, PROFILE_IMAGE, INTRO, TOTAL_CERTIFY_COUNT, BADGE_TYPES,
			0, 0, 0);
	}
}
