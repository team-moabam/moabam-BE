package com.moabam.api.application.ranking;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.ranking.PersonalRankingInfo;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfoResponse;
import com.moabam.api.dto.ranking.TopRankingResponses;
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	private static final String RANKING = "Ranking";
	private static final int START_INDEX = 0;
	private static final int LIMIT_INDEX = 10;

	private final ObjectMapper objectMapper;
	private final ZSetRedisRepository zSetRedisRepository;

	public void addRanking(RankingInfo rankingInfo, Long totalCertifyCount) {
		zSetRedisRepository.add(RANKING, rankingInfo, totalCertifyCount);
	}

	public void updateCacheScore(CertifiedMemberInfo info) {
		Member member = info.member();
		RankingInfo rankingInfo = MemberMapper.toRankingInfo(member);
		zSetRedisRepository.add(RANKING, rankingInfo, member.getTotalCertifyCount());
	}

	public void changeInfos(RankingInfo before, RankingInfo after) {
		zSetRedisRepository.changeMember(RANKING, before, after);
	}

	public void removeRanking(RankingInfo rankingInfo) {
		zSetRedisRepository.delete(RANKING, rankingInfo);
	}

	public TopRankingResponses getMemberRanking(PersonalRankingInfo myRankingInfo) {
		List<TopRankingInfoResponse> topRankings = getTopRankings();
		Long myRanking = zSetRedisRepository.reverseRank("Ranking", myRankingInfo);
		TopRankingInfoResponse myRankingInfoResponse = RankingMapper.topRankingResponse(myRanking.intValue(),
			myRankingInfo);

		return RankingMapper.topRankingResponses(myRankingInfoResponse, topRankings);
	}

	private List<TopRankingInfoResponse> getTopRankings() {
		Set<ZSetOperations.TypedTuple<Object>> topRankings =
			zSetRedisRepository.rangeJson(RANKING, START_INDEX, LIMIT_INDEX);

		Set<Long> scoreSet = new HashSet<>();
		List<TopRankingInfoResponse> topRankingInfoRespons = new ArrayList<>();

		for (ZSetOperations.TypedTuple<Object> topRanking : topRankings) {
			long score = requireNonNull(topRanking.getScore()).longValue();
			scoreSet.add(score);

			RankingInfo rankingInfo = objectMapper.convertValue(topRanking.getValue(), RankingInfo.class);
			topRankingInfoRespons.add(RankingMapper.topRankingResponse(scoreSet.size(), score, rankingInfo));
		}
		return topRankingInfoRespons;
	}
}
