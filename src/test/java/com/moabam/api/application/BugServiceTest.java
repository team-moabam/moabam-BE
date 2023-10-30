package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DisplayNameGenerator.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.factory.MemberFactory;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class BugServiceTest {

	@InjectMocks
	BugService bugService;

	@Mock
	MemberService memberService;

	@Test
	void 벌레를_조회한다() {
		// given
		Long memberId = 1L;
		Member member = MemberFactory.create(memberId);
		given(memberService.getById(memberId)).willReturn(member);

		// when
		BugResponse response = bugService.getBug(memberId);

		// then
		Bug bug = member.getBug();
		assertThat(response.morningBug()).isEqualTo(bug.getMorningBug());
		assertThat(response.nightBug()).isEqualTo(bug.getNightBug());
		assertThat(response.goldenBug()).isEqualTo(bug.getGoldenBug());
	}
}
