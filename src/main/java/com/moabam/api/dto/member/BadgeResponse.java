package com.moabam.api.dto.member;

import com.moabam.api.domain.member.BadgeType;

import lombok.Builder;

@Builder
public record BadgeResponse(
	String badge,
	boolean unlock
) {

}
