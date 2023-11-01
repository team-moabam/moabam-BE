package com.moabam.api.dto;

import com.moabam.api.domain.entity.Bug;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class BugMapper {

	public static BugResponse toBugResponse(Bug bug) {
		return BugResponse.builder()
			.morningBug(bug.getMorningBug())
			.nightBug(bug.getNightBug())
			.goldenBug(bug.getGoldenBug())
			.build();
	}
}
