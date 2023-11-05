package com.moabam.api.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record TodayCertificateRankResponse(
	int rank,
	Long memberId,
	String nickname,
	String profileImage,
	int contributionPoint,
	String awakeImage,
	String sleepImage,
	List<CertificationImageResponse> certificationImage
) {

}
