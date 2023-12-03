package com.moabam.api.domain.member;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.moabam.api.dto.member.BadgeResponse;

import lombok.Getter;

@Getter
public enum BadgeType {

	BIRTH(10, "탄생 축하 뱃지"),
	LEVEL10(100, "10레벨 뱃지"),
	LEVEL50(500, "50레벨 뱃지");

	private final long certifyCount;
	private final String korean;

	BadgeType(long certifyCount, String korean) {
		this.certifyCount = certifyCount;
		this.korean = korean;
	}

	public static List<BadgeResponse> memberBadgeMap(Set<BadgeType> badgeTypes) {
		return Arrays.stream(BadgeType.values())
			.map(badgeType -> BadgeResponse.builder()
				.badge(badgeType.korean)
				.unlock(badgeTypes.contains(badgeType))
				.build())
			.toList();
	}

	public static Optional<BadgeType> getBadgeFrom(long certifyCount) {
		return Arrays.stream(BadgeType.values())
			.filter(badgeType -> badgeType.certifyCount == certifyCount)
			.findFirst();
	}
}
