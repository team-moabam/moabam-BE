package com.moabam.api.application.bug;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.dto.bug.BugResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BugMapper {

	public static BugResponse toBugResponse(Bug bug) {
		return BugResponse.builder()
			.morningBug(bug.getMorningBug())
			.nightBug(bug.getNightBug())
			.goldenBug(bug.getGoldenBug())
			.build();
	}

	public static BugHistory toUseBugHistory(Long memberId, BugType bugType, int quantity) {
		return BugHistory.builder()
			.memberId(memberId)
			.bugType(bugType)
			.actionType(BugActionType.USE)
			.quantity(quantity)
			.build();
	}
}
