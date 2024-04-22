package com.moabam.api.application.member;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberReadService {

	private final MemberRepository memberRepository;
	private final MemberSearchRepository memberSearchRepository;
	private final ParticipantSearchRepository participantSearchRepository;

	public Member readMember(Long id) {
		return memberSearchRepository.findMember(id)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public Optional<Member> findMember(String socialId) {
		return memberRepository.findBySocialId(socialId);
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}

	public void validateMemberToDelete(Long memberId) {
		List<Participant> participants = memberSearchRepository.findParticipantByMemberId(memberId);

		if (!participants.isEmpty()) {
			throw new NotFoundException(MEMBER_NOT_FOUND);
		}
	}

	public MemberInfoSearchResponse readMemberInfos(Long searchId, boolean isMe) {
		List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(searchId, isMe);

		if (memberInfos.isEmpty()) {
			throw new BadRequestException(MEMBER_NOT_FOUND);
		}

		return MemberMapper.toMemberInfoSearchResponse(memberInfos);
	}

	public List<Member> findAllMembers() {
		return memberSearchRepository.findAllMembers();
	}

	public void validateParticipants(Long memberId) {
		List<Participant> participants = participantSearchRepository.findAllByMemberIdParticipant(memberId);

		if (!participants.isEmpty()) {
			throw new BadRequestException(NEED_TO_EXIT_ALL_ROOMS);
		}
	}

	public void validateNickname(String myName, String nickname) {
		if (Objects.isNull(nickname)) {
			return;
		}
		if (StringUtils.isBlank(nickname)) {
			throw new NotFoundException(NICKNAME_NOT_NULL);
		}
		if (!memberRepository.existsByNickname(nickname)) {
			return;
		}
		if (!myName.equals(nickname)) {
			throw new ConflictException(NICKNAME_CONFLICT);
		}
	}
}
