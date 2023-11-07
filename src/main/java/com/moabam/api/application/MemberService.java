package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.api.domain.repository.MemberSearchRepository;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberSearchRepository memberSearchRepository;

	public Member getById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public Member getManager(Long roomId) {
		return memberSearchRepository.findManager(roomId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}
}
