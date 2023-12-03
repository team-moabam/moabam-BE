package com.moabam.support.fixture;

import com.moabam.api.domain.member.Badge;
import com.moabam.api.domain.member.BadgeType;

public class BadgeFixture {

	public static Badge badge(Long memberId, BadgeType badgeType) {
		return Badge.builder()
			.memberId(memberId)
			.type(badgeType)
			.build();
	}
}
