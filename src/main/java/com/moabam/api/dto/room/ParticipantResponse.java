package com.moabam.api.dto.room;

import lombok.Builder;

@Builder
public record ParticipantResponse(
	Long memberId,
	String nickname,
	int contributionPoint,
	String profileImage
) {

}
