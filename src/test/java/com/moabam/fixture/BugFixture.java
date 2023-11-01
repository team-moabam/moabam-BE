package com.moabam.fixture;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.dto.BugMapper;
import com.moabam.api.dto.BugResponse;

public final class BugFixture {

	public static final int MORNING_BUG = 10;
	public static final int NIGHT_BUG = 20;
	public static final int GOLDEN_BUG = 30;

	public static Bug bug() {
		return Bug.builder()
			.morningBug(MORNING_BUG)
			.nightBug(NIGHT_BUG)
			.goldenBug(GOLDEN_BUG)
			.build();
	}

	public static BugResponse bugResponse() {
		return BugMapper.from(bug());
	}
}
