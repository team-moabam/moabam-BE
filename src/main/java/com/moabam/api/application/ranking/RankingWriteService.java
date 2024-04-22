package com.moabam.api.application.ranking;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingWriteService {

	private final ZSetRedisRepository zSetRedisRepository;

	public void addRanking(String key, RankingInfo rankingInfo, Long totalCertifyCount) {
		zSetRedisRepository.add(key, rankingInfo, totalCertifyCount);
	}

	public void updateScores(String key, List<UpdateRanking> updateRankings) {
		updateRankings.forEach(
			updateRanking -> zSetRedisRepository.add(key, updateRanking.rankingInfo(), updateRanking.score()));
	}

	public void changeInfos(String key, RankingInfo before, RankingInfo after) {
		zSetRedisRepository.changeMember(key, before, after);
	}

	public void removeRanking(String key, RankingInfo rankingInfo) {
		zSetRedisRepository.delete(key, rankingInfo);
	}
}
