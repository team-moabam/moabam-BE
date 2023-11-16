package com.moabam.api.application.bug;

import static com.moabam.api.domain.bug.BugActionType.*;
import static com.moabam.api.domain.bug.BugType.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.bug.repository.BugHistorySearchRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.TodayBugResponse;
import com.moabam.api.dto.bug.BugResponse;
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
		List<BugHistory> todayRewardBug = bugHistorySearchRepository.find(memberId, REWARD, clockHolder.times());
		int morningBug = calculateBugQuantity(todayRewardBug, MORNING);
		int nightBug = calculateBugQuantity(todayRewardBug, NIGHT);

		return BugMapper.toTodayBugResponse(morningBug, nightBug);
	}

	private int calculateBugQuantity(List<BugHistory> bugHistory, BugType bugType) {
		return bugHistory.stream()
			.filter(history -> bugType.equals(history.getBugType()))
			.mapToInt(BugHistory::getQuantity)
			.sum();
	}
}
