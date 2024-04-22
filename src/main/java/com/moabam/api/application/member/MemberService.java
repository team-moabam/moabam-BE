package com.moabam.api.application.member;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.application.ranking.RankingService;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.api.dto.member.ModifyMemberRequest;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthMember;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberReadService memberReadService;
	private final MemberWriteService memberWriteService;
	private final RankingService rankingService;
	private final FcmService fcmService;

	public Member findMember(Long memberId) {
		return memberReadService.readMember(memberId);
	}

	@Transactional
	public LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Member> member = memberReadService.findMember(String.valueOf(authorizationTokenInfoResponse.id()));
		Member loginMember = member.orElseGet(() -> signUp(authorizationTokenInfoResponse.id()));

		return AuthMapper.toLoginResponse(loginMember, member.isEmpty());
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberReadService.getRoomMembers(memberIds);
	}

	public void validateMemberToDelete(Long memberId) {
		memberReadService.validateMemberToDelete(memberId);
	}

	@Transactional
	public void delete(Member member) {
		memberReadService.validateParticipants(member.getId());
		memberWriteService.delete(member);
		rankingService.removeRanking(MemberMapper.toRankingInfo(member));
		fcmService.deleteTokenByMemberId(member.getId());
	}

	public MemberInfoResponse searchInfo(AuthMember authMember, Long memberId) {
		Long searchId = authMember.id();
		boolean isMe = confirmMe(searchId, memberId);

		if (!isMe) {
			searchId = memberId;
		}
		MemberInfoSearchResponse memberInfoSearchResponse = memberReadService.readMemberInfos(searchId, isMe);

		return MemberMapper.toMemberInfoResponse(memberInfoSearchResponse);
	}

	@Transactional
	public void modifyInfo(AuthMember authMember, ModifyMemberRequest modifyMemberRequest, String newProfileUri) {
		Member member = memberReadService.readMember(authMember.id());
		memberReadService.validateNickname(member.getNickname(), modifyMemberRequest.nickname());

		RankingInfo beforeInfo = MemberMapper.toRankingInfo(member);
		memberWriteService.changeInfo(member, modifyMemberRequest, newProfileUri);
		RankingInfo afterInfo = MemberMapper.toRankingInfo(member);
		rankingService.changeInfos(beforeInfo, afterInfo);
	}

	public UpdateRanking getRankingInfo(AuthMember authMember) {
		Member member = findMember(authMember.id());

		return MemberMapper.toUpdateRanking(member);
	}

	private Member signUp(Long socialId) {
		Member member = memberWriteService.signUp(socialId);
		rankingService.addRanking(MemberMapper.toRankingInfo(member), member.getTotalCertifyCount());

		return member;
	}

	private boolean confirmMe(Long myId, Long memberId) {
		return Objects.isNull(memberId) || myId.equals(memberId);
	}
}
