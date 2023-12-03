package com.moabam.support.fixture;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.PublicClaim;

public class PublicClaimFixture {

	public static final PublicClaim publicClaim() {
		return PublicClaim.builder()
			.id(1L)
			.nickname("nickname")
			.role(Role.USER)
			.build();
	}
}
