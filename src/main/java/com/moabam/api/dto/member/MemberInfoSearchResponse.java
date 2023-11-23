package com.moabam.api.dto.member;

import java.util.Set;

import com.moabam.api.domain.member.BadgeType;

import lombok.Builder;

@Builder
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

}
