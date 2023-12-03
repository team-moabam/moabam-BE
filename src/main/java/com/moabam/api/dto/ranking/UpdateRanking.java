package com.moabam.api.dto.ranking;

import lombok.Builder;

@Builder
public record UpdateRanking(
	RankingInfo rankingInfo,
	Long score
) {

}
