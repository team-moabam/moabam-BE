package com.moabam.support.fixture;

import com.moabam.api.domain.entity.Wallet;

public final class BugFixture {

	public static final int MORNING_BUG = 10;
	public static final int NIGHT_BUG = 20;
	public static final int GOLDEN_BUG = 30;

	public static Wallet bug() {
		return Wallet.builder()
			.morningBug(MORNING_BUG)
			.nightBug(NIGHT_BUG)
			.goldenBug(GOLDEN_BUG)
			.build();
	}
}
