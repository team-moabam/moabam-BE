package com.moabam.support.fixture;

import static com.moabam.support.fixture.ProductFixture.*;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.payment.Payment;

public final class BugFixture {

	public static final int MORNING_BUG = 10;
	public static final int NIGHT_BUG = 20;
	public static final int GOLDEN_BUG = 30;
	public static final int REWARD_MORNING_BUG = 3;
	public static final int USE_NIGHT_BUG = 5;

	public static Bug bug() {
		return Bug.builder()
			.morningBug(MORNING_BUG)
			.nightBug(NIGHT_BUG)
			.goldenBug(GOLDEN_BUG)
			.build();
	}

	public static BugHistory rewardMorningBugHistory(Long memberId) {
		return BugHistory.builder()
			.memberId(memberId)
			.bugType(BugType.MORNING)
			.actionType(BugActionType.REWARD)
			.quantity(REWARD_MORNING_BUG)
			.build();
	}

	public static BugHistory chargeGoldenBugHistory(Long memberId, Payment payment) {
		return BugHistory.builder()
			.memberId(memberId)
			.payment(payment)
			.bugType(BugType.GOLDEN)
			.actionType(BugActionType.CHARGE)
			.quantity(BUG_PRODUCT_QUANTITY)
			.build();
	}
}
