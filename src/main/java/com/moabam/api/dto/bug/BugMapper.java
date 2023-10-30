package com.moabam.api.dto.bug;

import com.moabam.api.domain.entity.Bug;

public final class BugMapper {

	public static BugResponse from(Bug bug) {
		return BugResponse.builder()
			.morningBug(bug.getMorningBug())
			.nightBug(bug.getNightBug())
			.goldenBug(bug.getGoldenBug())
			.build();
	}
}
