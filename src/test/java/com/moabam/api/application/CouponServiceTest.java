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

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.api.domain.repository.CouponRepository;
import com.moabam.api.dto.CreateCouponRequest;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.CouponFixture;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	@InjectMocks
	private CouponService couponService;

	@Mock
	private CouponRepository couponRepository;

	@DisplayName("쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void couponService_createCoupon() {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When
		couponService.createCoupon(1L, request);

		// Then
		verify(couponRepository).save(any(Coupon.class));
	}

	@DisplayName("중복된 쿠폰명을 발행한다. - ConflictException")
	@Test
	void couponService_createCoupon_ConflictException() {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		given(couponRepository.existsByName(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(1L, request))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_COUPON_NAME.getMessage());
	}

	@DisplayName("존재하지 않는 쿠폰 종류를 발행한다. - NotFoundException")
	@Test
	void couponService_createCoupon_NotFoundException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest("UNKNOWN", 1, 2);
		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(1L, request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_TYPE.getMessage());
	}

	@DisplayName("쿠폰 발급 종료 기간이 시작 기간보다 더 이전인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void couponService_createCoupon_BadRequestException() {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 2, 1);
		given(couponRepository.existsByName(any(String.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.createCoupon(1L, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@DisplayName("쿠폰 아이디와 일치하는 쿠폰을 삭제한다. - Void")
	@Test
	void couponService_deleteCoupon() {
		// Given
		Coupon coupon = CouponFixture.coupon(10, 100);
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		couponService.deleteCoupon(1L, 1L);

		// Then
		verify(couponRepository).delete(coupon);
	}

	@DisplayName("존재하지 않는 쿠폰 아이디를 삭제하려고 시도한다. - NotFoundException")
	@Test
	void couponService_deleteCoupon_NotFoundException() {
		// Given
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.deleteCoupon(1L, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}
}
