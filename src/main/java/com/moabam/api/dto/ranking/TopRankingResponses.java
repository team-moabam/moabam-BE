package com.moabam.api.dto.ranking;

import java.util.List;

import lombok.Builder;

@Builder
public record TopRankingResponses(
	List<TopRankingInfo> topRankings,
	TopRankingInfo myRanking
) {

}
