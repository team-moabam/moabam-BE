package com.moabam.api.dto.member;

import lombok.Builder;

@Builder
public record DeleteMemberResponse(
	String socialId,
	Long id
) {

}
