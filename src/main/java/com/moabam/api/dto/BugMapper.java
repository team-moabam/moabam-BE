package com.moabam.api.dto;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.domain.entity.BugHistory;
import com.moabam.api.domain.entity.enums.BugActionType;
import com.moabam.api.domain.entity.enums.BugType;

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
