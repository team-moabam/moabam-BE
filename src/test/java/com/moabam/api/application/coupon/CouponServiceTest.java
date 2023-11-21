package com.moabam.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import com.moabam.api.dto.coupon.CouponSearchRequest;
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
	void couponService_createCoupon() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When
		couponService.createCoupon(admin, request);

		// Then
		verify(couponRepository).save(any(Coupon.class));
	}

	@WithMember(role = Role.USER)
	@DisplayName("권한 없는 사용자가 쿠폰을 발행한다. - NotFoundException")
	@Test
	void couponService_createCoupon_Admin_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(admin, request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("중복된 쿠폰명을 발행한다. - ConflictException")
	@Test
	void couponService_createCoupon_ConflictException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		given(couponRepository.existsByName(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(admin, request))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_COUPON_NAME.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("존재하지 않는 쿠폰 종류를 발행한다. - NotFoundException")
	@Test
	void couponService_createCoupon_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		CreateCouponRequest request = CouponFixture.createCouponRequest("UNKNOWN", 1, 2);
		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(admin, request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_TYPE.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰 발급 종료 기간이 시작 기간보다 더 이전인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void couponService_createCoupon_BadRequestException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 2, 1);
		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(admin, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰 아이디와 일치하는 쿠폰을 삭제한다. - Void")
	@Test
	void couponService_deleteCoupon() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon(10, 100);
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		couponService.deleteCoupon(admin, 1L);

		// Then
		verify(couponRepository).delete(coupon);
	}

	@WithMember(role = Role.USER)
	@DisplayName("권한 없는 사용자가 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void couponService_deleteCoupon_Admin_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();

		// When & Then
		assertThatThrownBy(() -> couponService.deleteCoupon(admin, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("존재하지 않는 쿠폰 아이디를 삭제하려고 시도한다. - NotFoundException")
	@Test
	void couponService_deleteCoupon_NotFoundException() {
		// Given
		AuthMember admin = AuthorizationThreadLocal.getAuthMember();
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.deleteCoupon(admin, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}

	@DisplayName("특정 쿠폰을 조회한다. - CouponResponse")
	@Test
	void couponService_getCouponById() {
		// Given
		Coupon coupon = CouponFixture.coupon(10, 100);
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		CouponResponse actual = couponService.getCouponById(1L);

		// Then
		assertThat(actual.point()).isEqualTo(coupon.getPoint());
		assertThat(actual.stock()).isEqualTo(coupon.getStock());
	}

	@DisplayName("존재하지 않는 쿠폰을 조회한다. - NotFoundException")
	@Test
	void couponService_getCouponById_NotFoundException() {
		// Given
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.getCouponById(1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}

	@DisplayName("모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponService_getCoupons(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, true, true);
		given(couponSearchRepository.findAllByStatus(any(LocalDateTime.class), any(CouponSearchRequest.class)))
			.willReturn(coupons);
		given(clockHolder.times()).willReturn(LocalDateTime.now());

		// When
		List<CouponResponse> actual = couponService.getCoupons(request);

		// Then
		assertThat(actual).hasSize(coupons.size());
	}

	@DisplayName("해당 쿠폰은 발급 가능 기간입니다. - Coupon")
	@Test
	void couponService_validateCouponPeriod() {
		// Given
		LocalDateTime now = LocalDateTime.of(2023, 1, 1, 1, 0);
		Coupon coupon = CouponFixture.coupon("couponName", 1, 2);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.of(coupon));
		given(clockHolder.times()).willReturn(now);

		// When
		Coupon actual = couponService.validateCouponPeriod(coupon.getName());

		// Then
		assertThat(actual.getName()).isEqualTo(coupon.getName());
	}

	@DisplayName("해당 쿠폰은 발급 가능 기간이 아닙니다. - BadRequestException")
	@Test
	void couponService_validateCouponPeriod_BadRequestException() {
		// Given
		LocalDateTime now = LocalDateTime.of(2022, 1, 1, 1, 0);
		Coupon coupon = CouponFixture.coupon("couponName", 1, 2);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.of(coupon));
		given(clockHolder.times()).willReturn(now);

		// When & Then
		assertThatThrownBy(() -> couponService.validateCouponPeriod("couponName"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD_END.getMessage());
	}

	@DisplayName("해당 쿠폰은 존재하지 않습니다. - NotFoundException")
	@Test
	void couponService_validateCouponPeriod_NotFoundException() {
		// Given
		LocalDateTime now = LocalDateTime.of(2022, 1, 1, 1, 0);
		given(couponRepository.findByName(any(String.class))).willReturn(Optional.empty());
		given(clockHolder.times()).willReturn(now);

		// When & Then
		assertThatThrownBy(() -> couponService.validateCouponPeriod("Not found coupon name"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}
}
