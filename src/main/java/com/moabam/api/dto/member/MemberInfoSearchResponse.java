package com.moabam.api.dto.member;

import java.util.Set;

import com.moabam.api.domain.member.BadgeType;

public record MemberInfoSearchResponse(
	String nickname,
	String profileImage,
	String intro,
	long totalCertifyCount,
	Set<BadgeType> badges,
	Integer goldenBug,
	Integer morningBug,
	Integer nightBug
) {

	public MemberInfoSearchResponse(String nickname, String profileImage, String intro,
		long totalCertifyCount, Set<BadgeType> badges) {
		this(nickname, profileImage, intro, totalCertifyCount, badges, null, null, null);
	}
}
