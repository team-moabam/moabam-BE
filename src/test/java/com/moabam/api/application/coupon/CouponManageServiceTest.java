package com.moabam.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	CouponRepository couponRepository;

	@Mock
	CouponManageRepository couponManageRepository;

	@Mock
	CouponWalletRepository couponWalletRepository;

	@Mock
	ClockHolder clockHolder;

	@DisplayName("쿠폰 발행이 성공적으로 된다.")
	@Test
	void issue_all_success() {
		// Given
		Coupon coupon = CouponFixture.coupon(1000, 100);
		Set<Long> membersId = new HashSet<>(Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 1, 1, 1, 1));
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));
		given(couponManageRepository.getIssuedStock(any(String.class))).willReturn(10);
		given(couponManageRepository.popMinQueue(any(String.class), any(long.class))).willReturn(membersId);
		given(couponManageRepository.increaseIssuedStock(any(String.class))).willReturn(99);

		// When
		couponManageService.issue();

		// Then
		verify(couponWalletRepository, times(10)).save(any(CouponWallet.class));
	}

	@DisplayName("발행 가능한 쿠폰이 없다.")
	@Test
	void issue_notStartAt() {
		// Given
		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 1, 1, 1, 1));
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.empty());

		// When
		couponManageService.issue();

		// Then
		verify(couponManageRepository, times(0)).getIssuedStock(any(String.class));
		verify(couponManageRepository, times(0)).popMinQueue(any(String.class), any(long.class));
		verify(couponManageRepository, times(0)).increaseIssuedStock(any(String.class));
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
	}

	@DisplayName("해당 쿠폰은 재고가 마감된 쿠폰이다.")
	@Test
	void issue_stockEnd() {
		// Given
		Coupon coupon = CouponFixture.coupon(1000, 100);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 1, 1, 1, 1));
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));
		given(couponManageRepository.getIssuedStock(any(String.class))).willReturn(coupon.getStock());

		// When
		couponManageService.issue();

		// Then
		verify(couponManageRepository, times(0)).popMinQueue(any(String.class), any(long.class));
		verify(couponManageRepository, times(0)).increaseIssuedStock(any(String.class));
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
	}

	@DisplayName("대기열에 남은 인원이 모두 발급받지 못한다.")
	@Test
	void issue_queue_stockENd() {
		// Given
		Coupon coupon = CouponFixture.coupon(1000, 100);
		Set<Long> membersId = new HashSet<>(Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 1, 1, 1, 1));
		given(couponRepository.findByStartAt(any(LocalDate.class))).willReturn(Optional.of(coupon));
		given(couponManageRepository.getIssuedStock(any(String.class))).willReturn(10);
		given(couponManageRepository.popMinQueue(any(String.class), any(long.class))).willReturn(membersId);
		given(couponManageRepository.increaseIssuedStock(any(String.class))).willReturn(101);

		// When
		couponManageService.issue();

		// Then
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
	}

	@WithMember
	@DisplayName("쿠폰 발급 요청을 성공적으로 큐에 등록한다. - Void")
	@Test
	void register_success() {
		// Given
		AuthMember member = AuthorizationThreadLocal.getAuthMember();
		Coupon coupon = CouponFixture.coupon();

		given(clockHolder.times()).willReturn(LocalDateTime.now());
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

		given(clockHolder.times()).willReturn(LocalDateTime.now());
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

		given(clockHolder.times()).willReturn(LocalDateTime.now());
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
