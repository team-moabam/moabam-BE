package com.moabam.support.fixture;

import com.moabam.api.domain.entity.enums.Role;
import com.moabam.api.dto.PublicClaim;

public class PublicClaimFixture {

	public static final PublicClaim publicClaim() {
		return PublicClaim.builder()
			.id(1L)
			.nickname("nickname")
			.role(Role.USER)
			.build();
	}
}
