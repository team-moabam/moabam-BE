package com.moabam.api.dto.room;

import lombok.Builder;

@Builder
public record UnJoinedRoomCertificateRankResponse(
	int rank,
	Long memberId,
	String nickname,
	String awakeImage,
	String sleepImage
) {

}
