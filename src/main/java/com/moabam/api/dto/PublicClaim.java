package com.moabam.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moabam.api.domain.entity.enums.Role;

import lombok.Builder;

@Builder
public record PublicClaim(
	Long id,
	@JsonIgnore String nickname,
	@JsonIgnore Role role
) {

}
