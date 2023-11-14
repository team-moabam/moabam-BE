package com.moabam.api.dto.room;

import java.util.List;

import com.moabam.api.dto.room.CertificationImageResponse;

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
