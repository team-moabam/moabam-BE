package com.moabam.api.dto.room;

import lombok.Builder;

@Builder
public record TodayCertificateRankResponse(
	int rank,
	Long memberId,
	String nickname,
	boolean isNotificationSent,
	String profileImage,
	int contributionPoint,
	String awakeImage,
	String sleepImage,
	CertificationImagesResponse certificationImage
) {

}
