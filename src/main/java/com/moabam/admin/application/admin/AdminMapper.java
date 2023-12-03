package com.moabam.admin.application.admin;

import com.moabam.admin.domain.admin.Admin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminMapper {

	public static Admin toAdmin(Long socialId) {
		return Admin.builder()
			.socialId(String.valueOf(socialId))
			.build();
	}
}
