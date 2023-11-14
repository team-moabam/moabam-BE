package com.moabam.global.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moabam.api.domain.member.Role;

import lombok.Builder;

@Builder
public record PublicClaim(
	Long id,
	@JsonIgnore String nickname,
	@JsonIgnore Role role
) {

}
