package com.moabam.api.dto.member;

import com.moabam.api.domain.member.BadgeType;

public record MemberInfo(
	String nickname,
	String profileImage,
	String intro,
	long totalCertifyCount,
	BadgeType badges,
	Integer goldenBug,
	Integer morningBug,
	Integer nightBug
) {

	public MemberInfo(String nickname, String profileImage, String intro,
		long totalCertifyCount, BadgeType badges) {
		this(nickname, profileImage, intro, totalCertifyCount, badges, null, null, null);
	}
}
