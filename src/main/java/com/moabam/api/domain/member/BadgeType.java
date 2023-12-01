package com.moabam.api.domain.member;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.moabam.api.dto.member.BadgeResponse;

import lombok.Getter;

@Getter
public enum BadgeType {

	MORNING_BIRTH("MORNING", "오목눈이 탄생"),
	MORNING_ADULT("MORNING", "어른 오목눈이"),
	NIGHT_BIRTH("NIGHT", "부엉이 탄생"),
	NIGHT_ADULT("NIGHT", "어른 부엉이");

	private final String period;
	private final String korean;

	BadgeType(String period, String korean) {
		this.period = period;
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
}
