package com.moabam.api.application;

import static com.moabam.support.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.bug.BugResponse;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

	@InjectMocks
	BugService bugService;

	@Mock
	MemberService memberService;

	@DisplayName("벌레를 조회한다.")
	@Test
	void get_bug_success() {
		// given
		Long memberId = 1L;
		Member member = member();
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
