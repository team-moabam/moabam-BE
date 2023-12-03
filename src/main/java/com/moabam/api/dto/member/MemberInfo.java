package com.moabam.api.dto.member;

import com.moabam.api.domain.member.BadgeType;

public record MemberInfo(
	String nickname,
	String profileImage,
	String morningImage,
	String nightImage,
	String intro,
	long totalCertifyCount,
	BadgeType badges,
	Integer goldenBug,
	Integer morningBug,
	Integer nightBug
) {

	public MemberInfo(String nickname, String profileImage, String morningImage, String nightImage,
		String intro, long totalCertifyCount, BadgeType badges) {
		this(nickname, profileImage, morningImage, nightImage, intro,
			totalCertifyCount, badges, null, null, null);
	}
}
