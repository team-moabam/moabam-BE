package com.moabam.admin.application.admin;

import com.moabam.admin.domain.admin.Admin;

public class AdminMapper {

	public static Admin toAdmin(Long socialId) {
		return Admin.builder()
			.socialId(String.valueOf(socialId))
			.build();
	}
}
