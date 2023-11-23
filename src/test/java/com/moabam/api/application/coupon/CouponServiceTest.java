package com.moabam.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponSearchRepository;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.CouponFixture;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class CouponServiceTest {

	@InjectMocks
	private CouponService couponService;

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponSearchRepository couponSearchRepository;

	@Mock
	private ClockHolder clockHolder;

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void create() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

		// When
		couponService.create(admin, request);

		// Then
		verify(couponRepository).save(any(Coupon.class));
	}

	@WithMember(role = Role.USER)
	@DisplayName("권한 없는 사용자가 쿠폰을 발행한다. - NotFoundException")
	@Test
	void create_Admin_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		// When & Then
		assertThatThrownBy(() -> couponService.create(admin, request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("존재하지 않는 쿠폰 종류를 발행한다. - NotFoundException")
	@Test
	void create_Type_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest("UNKNOWN", 2, 1);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

		// When & Then
		assertThatThrownBy(() -> couponService.create(admin, request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_TYPE.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("중복된 쿠폰명을 발행한다. - ConflictException")
	@Test
	void create_ConflictException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(couponRepository.existsByName(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponService.create(admin, request))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_COUPON_NAME.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("현재 날짜가 쿠폰 발급 가능 날짜와 같거나 이후이다. - BadRequestException")
	@Test
	void create_StartAt_BadRequestException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(clockHolder.times()).willReturn(LocalDateTime.of(2025, 1, 1, 1, 1));
		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.create(admin, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_START_AT_PERIOD.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰 정보 오픈 날짜가 쿠폰 발급 시작 날짜와 같거나 이후인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void create_OpenAt_BadRequestException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		String couponType = CouponType.GOLDEN_COUPON.getName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 1);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

		// When & Then
		assertThatThrownBy(() -> couponService.create(admin, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_OPEN_AT_PERIOD.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰 아이디와 일치하는 쿠폰을 삭제한다. - Void")
	@Test
	void delete() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon(10, 100);
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		couponService.delete(admin, 1L);

		// Then
		verify(couponRepository).delete(coupon);
	}

	@WithMember(role = Role.USER)
	@DisplayName("권한 없는 사용자가 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void delete_Admin_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();

		// When & Then
		assertThatThrownBy(() -> couponService.delete(admin, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("존재하지 않는 쿠폰 아이디를 삭제하려고 시도한다. - NotFoundException")
	@Test
	void delete_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.delete(admin, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}

	@DisplayName("특정 쿠폰을 조회한다. - CouponResponse")
	@Test
	void getById() {
		// Given
		Coupon coupon = CouponFixture.coupon(10, 100);
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		CouponResponse actual = couponService.getById(1L);

		// Then
		assertThat(actual.point()).isEqualTo(coupon.getPoint());
		assertThat(actual.stock()).isEqualTo(coupon.getStock());
	}

	@DisplayName("존재하지 않는 쿠폰을 조회한다. - NotFoundException")
	@Test
	void getById_NotFoundException() {
		// Given
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.getById(1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}

	@DisplayName("모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, false);

		given(clockHolder.times()).willReturn(LocalDateTime.now());
		given(couponSearchRepository.findAllByStatus(any(LocalDate.class), any(CouponStatusRequest.class)))
			.willReturn(coupons);

		// When
		List<CouponResponse> actual = couponService.getAllByStatus(request);

		// Then
		assertThat(actual).hasSize(coupons.size());
	}

	@DisplayName("해당 쿠폰은 발급 가능 기간입니다. - Coupon")
	@Test
	void validatePeriod() {
		// Given
		LocalDateTime now = LocalDateTime.of(2023, 1, 1, 1, 0);
		Coupon coupon = CouponFixture.coupon("couponName", 1, 2);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.of(coupon));
		given(clockHolder.times()).willReturn(now);

		// When
		Coupon actual = couponService.validatePeriod(coupon.getName());

		// Then
		assertThat(actual.getName()).isEqualTo(coupon.getName());
	}

	@DisplayName("해당 쿠폰은 발급 가능 기간이 아닙니다. - BadRequestException")
	@Test
	void validatePeriod_BadRequestException() {
		// Given
		LocalDateTime now = LocalDateTime.of(2022, 1, 1, 1, 0);
		Coupon coupon = CouponFixture.coupon("couponName", 1, 2);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.of(coupon));
		given(clockHolder.times()).willReturn(now);

		// When & Then
		assertThatThrownBy(() -> couponService.validatePeriod("couponName"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@DisplayName("해당 쿠폰은 존재하지 않습니다. - NotFoundException")
	@Test
	void validatePeriod_NotFoundException() {
		// Given
		LocalDateTime now = LocalDateTime.of(2022, 1, 1, 1, 0);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.empty());
		given(clockHolder.times()).willReturn(now);

		// When & Then
		assertThatThrownBy(() -> couponService.validatePeriod("Not found coupon name"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}
}
