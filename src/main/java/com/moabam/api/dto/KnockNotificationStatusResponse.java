package com.moabam.api.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record KnockNotificationStatusResponse(
	List<Long> knockedMembersId,
	List<Long> notKnockedMembersId
) {

}
