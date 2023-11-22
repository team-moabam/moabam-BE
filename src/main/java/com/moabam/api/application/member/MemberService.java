package com.moabam.api.application.member;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.application.item.InventorySearchService;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.member.DeleteMemberResponse;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final InventorySearchService inventorySearchService;
	private final MemberSearchRepository memberSearchRepository;

	public Member getById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	@Transactional
	public LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Member> member = memberRepository.findBySocialId(String.valueOf(authorizationTokenInfoResponse.id()));
		Member loginMember = member.orElseGet(() -> signUp(authorizationTokenInfoResponse.id()));

		return AuthMapper.toLoginResponse(loginMember, member.isEmpty());
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}

	@Transactional
	public DeleteMemberResponse deleteMember(AuthMember authMember) {
		Member member = memberSearchRepository.findMemberNotManager(authMember.id())
			.orElseThrow(() -> new ConflictException(MEMBER_NOT_FOUND));

		String socialId = member.getSocialId();
		member.delete();

		return MemberMapper.toDeleteMemberResponse(member.getId(), socialId);
	}

	@Transactional
	public void undoDelete(DeleteMemberResponse deleteMemberResponse) {
		Member member = memberSearchRepository.findMember(deleteMemberResponse.id(), false)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		member.undoDelete(deleteMemberResponse.socialId());
	}

	private Member signUp(Long socialId) {
		Member member = MemberMapper.toMember(socialId);

		return memberRepository.save(member);
	}

	public MemberInfoResponse searchInfo(AuthMember authMember, Long memberId) {
		Long searchId = authMember.id();
		boolean isMe = confirmMe(searchId, memberId);

		if (!isMe) {
			searchId = memberId;
		}

		MemberInfoSearchResponse memberInfoSearchResponse = findMemberInfo(searchId, isMe);

		List<Inventory> inventories = inventorySearchService.getDefaultSkin(searchId);

		return MemberMapper.toMemberInfoResponse(memberInfoSearchResponse, inventories);
	}

	private MemberInfoSearchResponse findMemberInfo(Long searchId, boolean isMe) {
		List<MemberInfoSearchResponse> memberInfoSearchResponses =
			memberSearchRepository.findMemberAndBadges(searchId, isMe);

		if (memberInfoSearchResponses.isEmpty()) {
			throw new BadRequestException(MEMBER_NOT_FOUND);
		}

		return memberInfoSearchResponses.get(0);
	}

	private boolean confirmMe(Long myId, Long memberId) {
		return Objects.isNull(memberId) || myId.equals(memberId);
	}
}
