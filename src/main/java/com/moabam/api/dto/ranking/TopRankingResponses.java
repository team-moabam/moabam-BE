package com.moabam.api.dto.ranking;

import java.util.List;

import lombok.Builder;

@Builder
public record TopRankingResponse(
	List<TopRankingInfo> topRankings,
	TopRankingInfo myRanking
) {

}
