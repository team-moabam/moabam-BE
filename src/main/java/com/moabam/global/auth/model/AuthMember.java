package com.moabam.global.auth.model;

import com.moabam.api.domain.member.Role;

public record AuthMember(
	Long id,
	String nickname,
	Role role
) {

}
