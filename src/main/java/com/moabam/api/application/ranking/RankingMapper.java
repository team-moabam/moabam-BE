package com.moabam.api.application.ranking;

import java.util.List;

import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfoResponse;
import com.moabam.api.dto.ranking.TopRankingResponses;
import com.moabam.api.dto.ranking.UpdateRanking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingMapper {

	public static TopRankingInfoResponse topRankingResponse(int rank, long score, RankingInfo rankInfo) {
		return TopRankingInfoResponse.builder()
			.rank(rank)
			.score(score)
			.nickname(rankInfo.nickname())
			.image(rankInfo.image())
			.memberId(rankInfo.memberId())
			.build();
	}

	public static TopRankingInfoResponse topRankingResponse(int rank, UpdateRanking updateRanking) {
		return TopRankingInfoResponse.builder()
			.rank(rank)
			.score(updateRanking.score())
			.nickname(updateRanking.rankingInfo().nickname())
			.image(updateRanking.rankingInfo().image())
			.memberId(updateRanking.rankingInfo().memberId())
			.build();
	}

	public static TopRankingResponses topRankingResponses(TopRankingInfoResponse myRanking,
		List<TopRankingInfoResponse> topRankings) {
		return TopRankingResponses.builder()
			.topRankings(topRankings)
			.myRanking(myRanking)
			.build();
	}
}