package com.moabam.api.dto;

import com.moabam.api.domain.entity.Wallet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BugMapper {

	public static BugResponse toBugResponse(Wallet wallet) {
		return BugResponse.builder()
			.morningBug(wallet.getMorningBug())
			.nightBug(wallet.getNightBug())
			.goldenBug(wallet.getGoldenBug())
			.build();
	}

	public static Wallet toEntity(BugResponse response) {
		return Wallet.builder()
			.morningBug(response.morningBug())
			.nightBug(response.nightBug())
			.goldenBug(response.goldenBug())
			.build();
	}
}
