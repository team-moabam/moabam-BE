package com.moabam.api.application.ranking;

import java.util.List;

import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfo;
import com.moabam.api.dto.ranking.TopRankingResponse;
import com.moabam.api.dto.ranking.UpdateRanking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingMapper {

	public static TopRankingInfo topRankingResponse(int rank, long score, RankingInfo rankInfo) {
		return TopRankingInfo.builder()
			.rank(rank)
			.score(score)
			.nickname(rankInfo.nickname())
			.image(rankInfo.image())
			.memberId(rankInfo.memberId())
			.build();
	}

	public static TopRankingInfo topRankingResponse(int rank, UpdateRanking updateRanking) {
		return TopRankingInfo.builder()
			.rank(rank)
			.score(updateRanking.score())
			.nickname(updateRanking.rankingInfo().nickname())
			.image(updateRanking.rankingInfo().image())
			.memberId(updateRanking.rankingInfo().memberId())
			.build();
	}

	public static TopRankingResponse topRankingResponses(TopRankingInfo myRanking,
		List<TopRankingInfo> topRankings) {
		return TopRankingResponse.builder()
			.topRankings(topRankings)
			.myRanking(myRanking)
			.build();
	}
}
