package com.moabam.api.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.dto.bug.BugMapper;
import com.moabam.api.dto.bug.BugResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugService {

	private final MemberService memberService;

	public BugResponse getBug(Long memberId) {
		Member member = memberService.getById(memberId);
		return BugMapper.from(member.getBug());
	}
}
