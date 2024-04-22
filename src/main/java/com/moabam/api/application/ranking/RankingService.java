package com.moabam.api.application.ranking;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.application.member.MemberReadService;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfo;
import com.moabam.api.dto.ranking.TopRankingResponse;
import com.moabam.api.dto.ranking.UpdateRanking;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	private static final String RANKING = "Ranking";

	private final RankingReadService rankingReadService;
	private final RankingWriteService rankingWriteService;
	private final MemberReadService memberReadService;

	@Scheduled(cron = "0 11 * * * *")
	public void updateAllRanking() {
		List<Member> members = memberReadService.findAllMembers();
		List<UpdateRanking> updateRankings = members.stream()
			.map(MemberMapper::toUpdateRanking)
			.toList();

		updateScores(updateRankings);
	}

	public void addRanking(RankingInfo rankingInfo, Long totalCertifyCount) {
		rankingWriteService.addRanking(RANKING, rankingInfo, totalCertifyCount);
	}

	public void updateScores(List<UpdateRanking> updateRankings) {
		rankingWriteService.updateScores(RANKING, updateRankings);
	}

	public void changeInfos(RankingInfo before, RankingInfo after) {
		rankingWriteService.changeInfos(RANKING, before, after);
	}

	public void removeRanking(RankingInfo rankingInfo) {
		rankingWriteService.removeRanking(RANKING, rankingInfo);
	}

	public TopRankingResponse getMemberRanking(UpdateRanking myRankingInfo) {
		List<TopRankingInfo> topRankings = rankingReadService.readTopRankings(RANKING);
		Long myRanking = rankingReadService.readRank(RANKING, myRankingInfo.rankingInfo());

		if (Objects.isNull(myRanking)) {
			updateAllRanking();
		}

		Optional<TopRankingInfo> myTopRanking = topRankings.stream()
			.filter(topRankingInfo -> Objects.equals(topRankingInfo.memberId(), myRankingInfo.rankingInfo().memberId()))
			.findFirst();

		if (myTopRanking.isPresent()) {
			myRanking = (long)myTopRanking.get().rank();
		}

		TopRankingInfo myRankingInfoResponse = RankingMapper.topRankingResponse(myRanking, myRankingInfo);

		return RankingMapper.topRankingResponses(myRankingInfoResponse, topRankings);
	}
}
