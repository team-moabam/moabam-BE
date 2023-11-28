package com.moabam.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.notification.NotificationService;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponManageRepository;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.CouponFixture;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class CouponManageServiceTest {

	@InjectMocks
	CouponManageService couponManageService;

	@Mock
	NotificationService notificationService;

	@Mock
	CouponRepository couponRepository;

	@Mock
	CouponManageRepository couponManageRepository;

	@Mock
	CouponWalletRepository couponWalletRepository;

	@Mock
	ClockHolder clockHolder;

	@DisplayName("쿠폰 관리 인덱스를 성공적으로 초기화한다.")
	@Test
	void init_success() {
		// When & Then
		assertThatNoException().isThrownBy(() -> couponManageService.init());
	}

	@DisplayName("10명의 사용자가 쿠폰 발행을 성공적으로 한.")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideValues_Long")
	@ParameterizedTest
	void issue_all_success(Set<Long> values) {
		// Given
		Coupon coupon = CouponFixture.coupon(1000, 100);

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));
		given(couponManageRepository.range(any(String.class), any(long.class), any(long.class))).willReturn(values);
		given(couponManageRepository.increaseIssuedStock(any(String.class))).willReturn(100);

		// When
		couponManageService.issue();

		// Then
		verify(couponWalletRepository, times(10)).save(any(CouponWallet.class));
		verify(notificationService, times(10))
			.sendCouponIssueResult(any(Long.class), any(String.class), any(String.class));
	}

	@DisplayName("발행 가능한 쿠폰이 없다.")
	@Test
	void issue_notStartAt() {
		// Given
		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.empty());

		// When
		couponManageService.issue();

		// Then
		verify(couponManageRepository, times(0)).increaseIssuedStock(any(String.class));
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
		verify(couponManageRepository, times(0))
			.range(any(String.class), any(long.class), any(long.class));
		verify(notificationService, times(0))
			.sendCouponIssueResult(any(Long.class), any(String.class), any(String.class));
	}

	@DisplayName("해당 쿠폰은 재고가 마감된 쿠폰이다.")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideValues_Long")
	@ParameterizedTest
	void issue_stockEnd(Set<Long> values) {
		// Given
		Coupon coupon = CouponFixture.coupon(1000, 100);

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));
		given(couponManageRepository.range(any(String.class), any(long.class), any(long.class))).willReturn(values);
		given(couponManageRepository.increaseIssuedStock(any(String.class))).willReturn(101);

		// When
		couponManageService.issue();

		// Then
		verify(couponManageRepository, times(10)).increaseIssuedStock(any(String.class));
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
		verify(notificationService, times(10))
			.sendCouponIssueResult(any(Long.class), any(String.class), any(String.class));

	}

	@WithMember
	@DisplayName("쿠폰 발급 요청을 성공적으로 큐에 등록한다. - Void")
	@Test
	void register_success() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon();

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));

		// When
		couponManageService.register(member, coupon.getName());

		// Then
		verify(couponManageRepository).addIfAbsentQueue(any(String.class), any(Long.class), any(double.class));
	}

	@WithMember
	@DisplayName("금일 발급이 가능한 쿠폰이 없다. - BadRequestException")
	@Test
	void register_StartAt_BadRequestException() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponManageService.register(member, "couponName"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@WithMember
	@DisplayName("금일 발급 가능한 쿠폰의 이름과 일치하지 않는다. - BadRequestException")
	@Test
	void register_Name_BadRequestException() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon();

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));

		// When & Then
		assertThatThrownBy(() -> couponManageService.register(member, "Coupon Cannot Be Issued Today"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_PERIOD.getMessage());
	}

	@DisplayName("쿠폰 대기열과 발행된 재고가 정상적으로 삭제된다.")
	@Test
	void deleteCouponManage_success() {
		// Given
		String couponName = "couponName";

		// When
		couponManageService.deleteCouponManage(couponName);

		// Then
		verify(couponManageRepository).deleteQueue(couponName);
		verify(couponManageRepository).deleteIssuedStock(couponName);
	}

	@DisplayName("쿠폰 대기열이 정상적으로 삭제되지 않는다.")
	@Test
	void deleteCouponManage_Queue_NullPointerException() {
		// Given
		willThrow(NullPointerException.class).given(couponManageRepository).deleteQueue(any(String.class));

		// When & Then
		assertThatThrownBy(() -> couponManageService.deleteCouponManage("null"))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰의 발행된 재고가 정상적으로 삭제되지 않는다.")
	@Test
	void deleteCouponManage_Stock_NullPointerException() {
		// Given
		willDoNothing().given(couponManageRepository).deleteQueue(any(String.class));
		willThrow(NullPointerException.class).given(couponManageRepository).deleteIssuedStock(any(String.class));

		// When & Then
		assertThatThrownBy(() -> couponManageService.deleteCouponManage("null"))
			.isInstanceOf(NullPointerException.class);
	}
}
