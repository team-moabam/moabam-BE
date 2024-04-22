package com.moabam.api.application.ranking;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfo;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingReadService {

	private static final int START_INDEX = 0;
	private static final int LIMIT_INDEX = 9;

	private final ObjectMapper objectMapper;
	private final ZSetRedisRepository zSetRedisRepository;

	public Long readRank(String key, RankingInfo rankingInfo) {
		return zSetRedisRepository.reverseRank(key, rankingInfo) + 1;
	}

	public List<TopRankingInfo> readTopRankings(String key) {
		Set<ZSetOperations.TypedTuple<Object>> topRankings = zSetRedisRepository.rangeJson(key, START_INDEX,
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
