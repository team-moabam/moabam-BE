package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.LoginResponse;
import com.moabam.support.fixture.AuthorizationResponseFixture;
import com.moabam.support.fixture.MemberFixture;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	MemberService memberService;

	@Mock
	MemberRepository memberRepository;

	@DisplayName("회원 존재하고 로그인 성공")
	@Test
	void member_exist_and_login_success() {
		// given
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationResponseFixture.authorizationTokenInfoResponse();
		Member member = MemberFixture.member();
		willReturn(Optional.of(member))
			.given(memberRepository).findBySocialId(authorizationTokenInfoResponse.id());

		// when
		LoginResponse result = memberService.login(authorizationTokenInfoResponse);

		// then
		assertThat(result.publicClaim().id()).isEqualTo(member.getId());
		assertThat(result.isSignUp()).isFalse();
	}

	@DisplayName("회원가입 성공")
	@Test
	void signUp_success() {
		// given
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationResponseFixture.authorizationTokenInfoResponse();
		willReturn(Optional.empty())
			.given(memberRepository).findBySocialId(authorizationTokenInfoResponse.id());

		Member member = spy(MemberFixture.member());
		given(member.getId()).willReturn(1L);
		willReturn(member)
			.given(memberRepository).save(any(Member.class));

		// when
		LoginResponse result = memberService.login(authorizationTokenInfoResponse);

		// then
		assertThat(authorizationTokenInfoResponse.id()).isEqualTo(result.publicClaim().id());
		assertThat(result.isSignUp()).isTrue();
	}
}
