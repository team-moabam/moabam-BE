package com.moabam.api.dto.notification;

import java.util.List;

import lombok.Builder;

@Builder
public record KnockNotificationStatusResponse(
	List<Long> knockedMembersId,
	List<Long> notKnockedMembersId
) {

}
