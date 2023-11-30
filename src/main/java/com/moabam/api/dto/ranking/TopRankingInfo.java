package com.moabam.api.dto.ranking;

import lombok.Builder;

@Builder
public record TopRankingInfo(
	int rank,
	Long memberId,
	Long score,
	String nickname,
	String image
) {

}
