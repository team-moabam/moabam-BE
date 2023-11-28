package com.moabam.api.application.ranking;

import org.springframework.stereotype.Service;

import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	private final ZSetRedisRepository zSetRedisRepository;

	public void addRanking(RankingInfo rankingInfo, Long totalCertifyCount) {
		zSetRedisRepository.add("Ranking", rankingInfo, totalCertifyCount);
	}

	public void updateCacheScore(CertifiedMemberInfo info) {
		Member member = info.member();
		RankingInfo rankingInfo = MemberMapper.toRankingInfo(member);
		zSetRedisRepository.add("Ranking", rankingInfo, member.getTotalCertifyCount());
	}

	public void changeInfos(RankingInfo before, RankingInfo after) {
		zSetRedisRepository.changeMember("Ranking", before, after);
	}

	public void removeRanking(RankingInfo rankingInfo) {
		zSetRedisRepository.delete("Ranking", rankingInfo);
	}
}
