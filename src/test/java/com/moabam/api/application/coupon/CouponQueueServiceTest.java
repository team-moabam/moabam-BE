package com.moabam.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.repository.CouponQueueRepository;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.CouponFixture;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class CouponQueueServiceTest {

	@InjectMocks
	private CouponQueueService couponQueueService;

	@Mock
	private CouponQueueRepository couponQueueRepository;

	@Mock
	private CouponService couponService;

	@WithMember
	@DisplayName("쿠폰 발급 요청을 성공적으로 큐에 등록한다. - Void")
	@Test
	void register() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon();

		given(couponService.validatePeriod(any(String.class))).willReturn(coupon);
		given(couponQueueRepository.rank(any(String.class), any(Long.class))).willReturn((long)coupon.getStock());

		// When
		couponQueueService.register(member, coupon.getName());

		// Then
		verify(couponQueueRepository).addIfAbsent(any(String.class), any(Long.class), any(double.class));
	}

	@WithMember
	@DisplayName("해당 쿠폰은 발급 가능 기간이 아니다. - BadRequestException")
	@Test
	void register_StartAt_BadRequestException() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();

		given(couponService.validatePeriod(any(String.class)))
			.willThrow(new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD));

		// When & Then
		verify(couponQueueRepository, times(0)).addIfAbsent(any(String.class), any(Long.class), any(double.class));
		assertThatThrownBy(() -> couponQueueService.register(member, "couponName"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@WithMember
	@DisplayName("해당 쿠폰은 선착순이 마감된 쿠폰이다. - BadRequestException")
	@Test
	void register_Stock_End_BadRequestException() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon();

		given(couponService.validatePeriod(any(String.class))).willReturn(coupon);
		given(couponQueueRepository.rank(any(String.class), any(Long.class))).willReturn(coupon.getStock() + 1L);

		// When & Then
		verify(couponQueueRepository, times(0)).addIfAbsent(any(String.class), any(Long.class), any(double.class));
		assertThatThrownBy(() -> couponQueueService.register(member, coupon.getName()))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_STOCK_END.getMessage());
	}
}
