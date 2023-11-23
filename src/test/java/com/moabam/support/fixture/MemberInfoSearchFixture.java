package com.moabam.support.fixture;

import java.util.List;

import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.dto.member.MemberInfo;

public class MemberInfoSearchFixture {

	private static final String NICKNAME = "nickname";
	private static final String PROFILE_IMAGE = "profileuri";
	private static final String INTRO = "intro";
	private static final long TOTAL_CERTIFY_COUNT = 15;

	public static List<MemberInfo> friendMemberInfo() {
		return friendMemberInfo(TOTAL_CERTIFY_COUNT);
	}

	public static List<MemberInfo> friendMemberInfo(long total) {
		return List.of(
			new MemberInfo(NICKNAME, PROFILE_IMAGE, INTRO, total, BadgeType.MORNING_BIRTH,
				0, 0, 0),
			new MemberInfo(NICKNAME, PROFILE_IMAGE, INTRO, total, BadgeType.NIGHT_BIRTH,
				0, 0, 0)
		);
	}

	public static List<MemberInfo> myInfo() {
		return List.of(
			new MemberInfo(NICKNAME, PROFILE_IMAGE, INTRO, TOTAL_CERTIFY_COUNT, BadgeType.MORNING_BIRTH,
				0, 0, 0),
			new MemberInfo(NICKNAME, PROFILE_IMAGE, INTRO, TOTAL_CERTIFY_COUNT, BadgeType.NIGHT_BIRTH,
				0, 0, 0)
		);
	}
}
