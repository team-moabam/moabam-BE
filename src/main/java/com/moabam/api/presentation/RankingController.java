package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.ranking.RankingService;
import com.moabam.api.dto.ranking.TopRankingResponses;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import lombok.RequiredArgsConstructor;

@RequestMapping("/rankings")
@RestController
@RequiredArgsConstructor
public class RankingController {

	private final RankingService rankingService;
	private final MemberService memberService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public TopRankingResponses getRanking(@Auth AuthMember authMember) {
		UpdateRanking rankingInfo = memberService.getRankingInfo(authMember);

		return rankingService.getMemberRanking(rankingInfo);
	}
}
