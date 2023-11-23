package com.moabam.api.application.member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.item.InventorySearchService;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.AuthorizationResponseFixture;
import com.moabam.support.fixture.InventoryFixture;
import com.moabam.support.fixture.ItemFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.MemberInfoSearchFixture;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class MemberServiceTest {

	@InjectMocks
	MemberService memberService;

	@Mock
	InventorySearchService inventorySearchService;

	@Mock
	MemberRepository memberRepository;

	@Mock
	MemberSearchRepository memberSearchRepository;

	@Mock
	ClockHolder clockHolder;

	@DisplayName("회원 존재하고 로그인 성공")
	@Test
	void member_exist_and_login_success() {
		// given
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationResponseFixture.authorizationTokenInfoResponse();
		Member member = MemberFixture.member();
		willReturn(Optional.of(member))
			.given(memberRepository).findBySocialId(String.valueOf(authorizationTokenInfoResponse.id()));

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
			.given(memberRepository).findBySocialId(String.valueOf(authorizationTokenInfoResponse.id()));

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

	@DisplayName("회원 삭제 성공")
	@Test
	void undo_delete_member(@WithMember AuthMember authMember) {
		// given
		Member member = MemberFixture.member();
		given(clockHolder.times()).willReturn(LocalDateTime.now());

		// When
		memberService.delete(member);

		// then
		assertThat(member).isNotNull();
		assertThat(member.getSocialId()).contains("delete");
	}

	@DisplayName("내 회원 정보가 없어서 예외 발생")
	@Test
	void search_my_info_failBy_member_null(@WithMember AuthMember authMember) {
		// given
		given(memberSearchRepository.findMemberAndBadges(authMember.id(), true))
			.willReturn(List.of());

		// When + Then
		assertThatThrownBy(() -> memberService.searchInfo(authMember, null))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@DisplayName("친구 회원 정보가 없어서 예외 발생")
	@Test
	void search_friend_info_failBy_member_null(@WithMember AuthMember authMember) {
		// given
		given(memberSearchRepository.findMemberAndBadges(123L, false))
			.willReturn(List.of());

		// When + Then
		assertThatThrownBy(() -> memberService.searchInfo(authMember, 123L))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@DisplayName("내 기본 스킨 2개가 없을 때 예외 발생")
	@Test
	void search_my_info_success(@WithMember AuthMember authMember) {
		// Given
		long total = 36;
		Item night = ItemFixture.nightMageSkin();
		Item morning = ItemFixture.morningSantaSkin().build();

		given(memberSearchRepository.findMemberAndBadges(authMember.id(), true))
			.willReturn(List.of(MemberInfoSearchFixture.friendMemberInfo(total)));
		given(inventorySearchService.getDefaultSkin(authMember.id()))
			.willReturn(List.of(
				InventoryFixture.inventory(authMember.id(), morning),
				InventoryFixture.inventory(authMember.id(), night)));

		// When + Then
		MemberInfoResponse memberInfoResponse = memberService.searchInfo(authMember, null);

		assertAll(
			() -> assertThat(memberInfoResponse.exp()).isEqualTo(total % 10),
			() -> assertThat(memberInfoResponse.level()).isEqualTo(total / 10)
		);
	}
}
