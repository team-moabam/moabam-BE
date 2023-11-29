package com.moabam.api.application.coupon;

import static com.moabam.support.fixture.CouponFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
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

import com.moabam.api.application.bug.BugService;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponSearchRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletSearchRepository;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.api.dto.coupon.MyCouponResponse;
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
	CouponService couponService;

	@Mock
	BugService bugService;

	@Mock
	CouponManageService couponManageService;

	@Mock
	CouponRepository couponRepository;

	@Mock
	CouponWalletRepository couponWalletRepository;

	@Mock
	CouponSearchRepository couponSearchRepository;

	@Mock
	CouponWalletSearchRepository couponWalletSearchRepository;

	@Mock
	ClockHolder clockHolder;

	@DisplayName("관리자가 쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void create_success() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When
		couponService.create(request, 1L, Role.ADMIN);

		// Then
		verify(couponRepository).save(any(Coupon.class));
	}

	@DisplayName("권한 없는 사용자가 쿠폰을 발행한다. - NotFoundException")
	@Test
	void create_Admin_NotFoundException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.USER))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@DisplayName("존재하지 않는 쿠폰 종류를 발행한다. - NotFoundException")
	@Test
	void create_Type_NotFoundException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest("UNKNOWN", 2, 1);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.ADMIN))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_TYPE.getMessage());
	}

	@DisplayName("중복된 쿠폰명을 발행한다. - ConflictException")
	@Test
	void create_Name_ConflictException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(couponRepository.existsByName(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.ADMIN))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_COUPON_NAME.getMessage());
	}

	@DisplayName("중복된 쿠폰 발행 가능 날짜를 발행한다. - ConflictException")
	@Test
	void create_StartAt_ConflictException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(couponRepository.existsByStartAt(any(LocalDate.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.ADMIN))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_COUPON_START_AT.getMessage());
	}

	@DisplayName("현재 날짜가 쿠폰 발급 가능 날짜와 같거나 이후이다. - BadRequestException")
	@Test
	void create_StartAt_BadRequestException() {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(clockHolder.date()).willReturn(LocalDate.of(2025, 1, 1));
		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(couponRepository.existsByStartAt(any(LocalDate.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.ADMIN))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_START_AT_PERIOD.getMessage());
	}

	@DisplayName("쿠폰 정보 오픈 날짜가 쿠폰 발급 시작 날짜와 같거나 이후인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void create_OpenAt_BadRequestException() {
		// Given
		String couponType = CouponType.GOLDEN.getName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 1);

		given(couponRepository.existsByName(any(String.class))).willReturn(false);
		given(couponRepository.existsByStartAt(any(LocalDate.class))).willReturn(false);
		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		assertThatThrownBy(() -> couponService.create(request, 1L, Role.ADMIN))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_OPEN_AT_PERIOD.getMessage());
	}

	@DisplayName("쿠폰 아이디와 일치하는 쿠폰을 성공적으로 삭제한다. - Void")
	@Test
	void delete_success() {
		// Given
		Coupon coupon = CouponFixture.coupon(10, 100);

		given(couponRepository.findById(any(Long.class))).willReturn(Optional.of(coupon));

		// When
		couponService.delete(1L, Role.ADMIN);

		// Then
		verify(couponRepository).delete(coupon);
		verify(couponManageService).deleteQueue(any(String.class));
	}

	@DisplayName("권한 없는 사용자가 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void delete_Admin_NotFoundException() {
		// When & Then
		assertThatThrownBy(() -> couponService.delete(1L, Role.USER))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.MEMBER_NOT_FOUND.getMessage());
	}

	@DisplayName("존재하지 않는 쿠폰 아이디를 삭제하려고 시도한다. - NotFoundException")
	@Test
	void delete_NotFoundException() {
		// Given
		given(couponRepository.findById(any(Long.class))).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.delete(1L, Role.ADMIN))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON.getMessage());
	}

	@DisplayName("특정 쿠폰을 성공적으로 조회한다. - CouponResponse")
	@Test
	void getById_success() {
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

	@DisplayName("모든 쿠폰을 성공적으로 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus_success(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, false);

		given(clockHolder.date()).willReturn(LocalDate.now());
		given(couponSearchRepository.findAllByStatus(any(LocalDate.class), any(CouponStatusRequest.class)))
			.willReturn(coupons);

		// When
		List<CouponResponse> actual = couponService.getAllByStatus(request);

		// Then
		assertThat(actual).hasSize(coupons.size());
	}

	@WithMember
	@DisplayName("나의 쿠폰함을 성공적으로 조회한다.")
	@MethodSource("com.moabam.support.fixture.CouponWalletFixture#provideCouponWalletByCouponId1_total5")
	@ParameterizedTest
	void getWallet_success(List<CouponWallet> couponWallets) {
		// Given
		AuthMember authMember = AuthorizationThreadLocal.getAuthMember();

		given(couponWalletSearchRepository.findAllByCouponIdAndMemberId(isNull(), any(Long.class)))
			.willReturn(couponWallets);

		// When
		List<MyCouponResponse> actual = couponService.getWallet(null, authMember);

		// Then
		assertThat(actual).hasSize(couponWallets.size());
	}

	@WithMember
	@DisplayName("지갑에서 특정 쿠폰을 성공적으로 조회한다.")
	@Test
	void getByWalletIdAndMemberId_success() {
		// Given
		CouponWallet couponWallet = CouponWallet.create(1L, coupon());

		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.of(couponWallet));

		// When
		CouponWallet actual = couponService.getWalletByIdAndMemberId(1L, 1L);

		// Then
		assertThat(actual.getCoupon().getName()).isEqualTo(couponWallet.getCoupon().getName());
	}

	@DisplayName("특정 회원이 쿠폰 지갑에 가지고 있는 특정 쿠폰을 성공적으로 사용한다. - Void")
	@Test
	void use_success() {
		// Given
		Coupon coupon = CouponFixture.coupon(CouponType.GOLDEN, 1000);
		CouponWallet couponWallet = CouponWallet.create(1L, coupon);

		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.of(couponWallet));

		// When
		couponService.use(1L, 1L);

		// Then
		verify(couponWalletRepository).delete(any(CouponWallet.class));
		verify(bugService).applyCoupon(any(Long.class), any(BugType.class), any(int.class));
	}

	@DisplayName("특정 회원이 쿠폰 지갑에 가지고 있는 할인 쿠폰을 사용한다. - BadRequestException")
	@Test
	void use_BadRequestException() {
		// Given
		Coupon coupon = CouponFixture.coupon(CouponType.DISCOUNT, 1000);
		CouponWallet couponWallet = CouponWallet.create(1L, coupon);

		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.of(couponWallet));

		// When & Then
		assertThatThrownBy(() -> couponService.use(1L, 1L))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_DISCOUNT_COUPON.getMessage());
	}

	@DisplayName("특정 회원이 쿠폰 지갑에 가지고 있지 않은 쿠폰을 사용한다. - NotFoundException")
	@Test
	void use_NotFoundException() {
		// Given
		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> couponService.use(1L, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_WALLET.getMessage());
	}

	@DisplayName("결제할 때, 할인 쿠폰을 사용한다. - Void")
	@Test
	void discount_success() {
		// Given
		Coupon coupon = CouponFixture.coupon(CouponType.DISCOUNT, 1000);
		CouponWallet couponWallet = CouponWallet.create(1L, coupon);

		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.of(couponWallet));

		// When
		couponService.discount(1L, 1L);

		// Then
		verify(couponWalletRepository).delete(couponWallet);
	}

	@DisplayName("결제할 때, 벌레 쿠폰을 사용한다. - BadRequestException")
	@Test
	void discount_BadRequestException() {
		// Given
		Coupon coupon = CouponFixture.coupon(CouponType.GOLDEN, 1000);
		CouponWallet couponWallet = CouponWallet.create(1L, coupon);

		given(couponWalletSearchRepository.findByIdAndMemberId(any(Long.class), any(Long.class)))
			.willReturn(Optional.of(couponWallet));

		// When & Then
		assertThatThrownBy(() -> couponService.discount(1L, 1L))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_BUG_COUPON.getMessage());
	}
}
