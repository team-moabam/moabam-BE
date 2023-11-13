package com.moabam.api.dto;

import com.moabam.api.domain.entity.enums.Role;

public record AuthorizationMember(
	Long id,
	String nickname,
	Role role
) {

}
