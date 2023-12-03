package com.moabam.api.application.ranking;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfo;
import com.moabam.api.dto.ranking.TopRankingResponse;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	private static final String RANKING = "Ranking";
	private static final int START_INDEX = 0;
	private static final int LIMIT_INDEX = 9;

	private final ObjectMapper objectMapper;
	private final ZSetRedisRepository zSetRedisRepository;

	public void addRanking(RankingInfo rankingInfo, Long totalCertifyCount) {
		zSetRedisRepository.add(RANKING, rankingInfo, totalCertifyCount);
	}

	public void updateScores(List<UpdateRanking> updateRankings) {
		updateRankings.forEach(
			updateRanking -> zSetRedisRepository.add(RANKING, updateRanking.rankingInfo(), updateRanking.score()));
	}

	public void changeInfos(RankingInfo before, RankingInfo after) {
		zSetRedisRepository.changeMember(RANKING, before, after);
	}

	public void removeRanking(RankingInfo rankingInfo) {
		zSetRedisRepository.delete(RANKING, rankingInfo);
	}

	public TopRankingResponse getMemberRanking(UpdateRanking myRankingInfo) {
		List<TopRankingInfo> topRankings = getTopRankings();
		Long myRanking = zSetRedisRepository.reverseRank(RANKING, myRankingInfo.rankingInfo());

		Optional<TopRankingInfo> myTopRanking = topRankings.stream()
			.filter(topRankingInfo -> Objects.equals(topRankingInfo.memberId(), myRankingInfo.rankingInfo().memberId()))
			.findFirst();

		if (myTopRanking.isPresent()) {
			myRanking = (long)myTopRanking.get().rank();
		}

		TopRankingInfo myRankingInfoResponse = RankingMapper.topRankingResponse(myRanking.intValue(), myRankingInfo);

		return RankingMapper.topRankingResponses(myRankingInfoResponse, topRankings);
	}

	private List<TopRankingInfo> getTopRankings() {
		Set<ZSetOperations.TypedTuple<Object>> topRankings = zSetRedisRepository.rangeJson(RANKING, START_INDEX,
			LIMIT_INDEX);

		Set<Long> scoreSet = new HashSet<>();
		List<TopRankingInfo> topRankingInfo = new ArrayList<>();

		for (ZSetOperations.TypedTuple<Object> topRanking : topRankings) {
			long score = requireNonNull(topRanking.getScore()).longValue();
			scoreSet.add(score);

			RankingInfo rankingInfo = objectMapper.convertValue(topRanking.getValue(), RankingInfo.class);
			topRankingInfo.add(RankingMapper.topRankingResponse(scoreSet.size(), score, rankingInfo));
		}

		return topRankingInfo;
	}
}
