package com.moabam.api.dto.ranking;

import lombok.Builder;

@Builder
public record PersonalRankingInfo(
	Long memberId,
	String nickname,
	String image,
	Long score
) {

}
