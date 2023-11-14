package com.moabam.support.fixture;

import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;

public final class BugHistoryFixture {

	public static BugHistory rewardMorningBug(Long memberId, int quantity) {
		return BugHistory.builder()
			.memberId(memberId)
			.bugType(BugType.MORNING)
			.actionType(BugActionType.REWARD)
			.quantity(quantity)
			.build();
	}

	public static BugHistory rewardNightBug(Long memberId, int quantity) {
		return BugHistory.builder()
			.memberId(memberId)
			.bugType(BugType.NIGHT)
			.actionType(BugActionType.REWARD)
			.quantity(quantity)
			.build();
	}
}
