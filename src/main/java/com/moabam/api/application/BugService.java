package com.moabam.api.application;

import static com.moabam.global.common.util.StreamUtils.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.BugHistory;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.enums.BugActionType;
import com.moabam.api.domain.entity.enums.BugType;
import com.moabam.api.domain.repository.BugHistorySearchRepository;
import com.moabam.api.dto.BugMapper;
import com.moabam.api.dto.BugResponse;
import com.moabam.api.dto.TodayBugResponse;
import com.moabam.global.common.util.ClockHolder;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugService {

	private final MemberService memberService;
	private final BugHistorySearchRepository bugHistorySearchRepository;
	private final ClockHolder clockHolder;

	public BugResponse getBug(Long memberId) {
		Member member = memberService.getById(memberId);

		return BugMapper.toBugResponse(member.getBug());
	}

	public TodayBugResponse getTodayBug(Long memberId) {
		List<BugHistory> morningBugHistory = bugHistorySearchRepository
			.find(memberId, BugType.MORNING, BugActionType.REWARD, clockHolder.times());
		List<BugHistory> nightBugHistory = bugHistorySearchRepository
			.find(memberId, BugType.NIGHT, BugActionType.REWARD, clockHolder.times());

		return BugMapper.toTodayBugResponse(sum(morningBugHistory, BugHistory::getQuantity),
			sum(nightBugHistory, BugHistory::getQuantity));
	}
}
