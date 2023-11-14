package com.moabam.global.auth.model;

import com.moabam.api.domain.member.Role;

public record AuthorizationMember(
	Long id,
	String nickname,
	Role role
) {

}
