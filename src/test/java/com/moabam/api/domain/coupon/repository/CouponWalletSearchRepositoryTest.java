package com.moabam.api.domain.coupon.repository;

import static com.moabam.support.fixture.CouponFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.CouponFixture;

@QuerydslRepositoryTest
class CouponWalletSearchRepositoryTest {

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponWalletRepository couponWalletRepository;

	@Autowired
	private CouponWalletSearchRepository couponWalletSearchRepository;

	@DisplayName("나의 쿠폰함의 특정 쿠폰을 조회한다.. - List<CouponWallet>")
	@Test
	void findAllByCouponIdAndMemberId_success() {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon());
		CouponWallet couponWallet = couponWalletRepository.save(CouponWallet.create(1L, coupon));

		// When
		List<CouponWallet> actual = couponWalletSearchRepository.findAllByCouponIdAndMemberId(coupon.getId(), 1L);

		// Then
		assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getCoupon().getName()).isEqualTo(coupon.getName());
		assertThat(actual.get(0).getMemberId()).isEqualTo(couponWallet.getMemberId());
	}

	@DisplayName("ID가 1인 회원은 쿠폰 1개를 가지고 있다. - List<CouponWallet>")
	@MethodSource("com.moabam.support.fixture.CouponWalletFixture#provideCouponWalletAll")
	@ParameterizedTest
	void findAllByCouponIdAndMemberId_Id1_success(List<CouponWallet> couponWallets) {
		// Given
		couponWallets.forEach(couponWallet -> {
			Coupon coupon = couponRepository.save(couponWallet.getCoupon());
			couponWalletRepository.save(CouponWallet.create(couponWallet.getMemberId(), coupon));
		});

		// When
		List<CouponWallet> actual = couponWalletSearchRepository.findAllByCouponIdAndMemberId(null, 1L);

		// Then
		assertThat(actual).hasSize(1);
	}

	@DisplayName("ID가 2인 회원은 쿠폰 ID가 777인 쿠폰을 가지고 있지 않다. - List<CouponWallet>")
	@MethodSource("com.moabam.support.fixture.CouponWalletFixture#provideCouponWalletAll")
	@ParameterizedTest
	void findAllByCouponIdAndMemberId_Id2_notCouponId777(List<CouponWallet> couponWallets) {
		// Given
		couponWallets.forEach(couponWallet -> {
			Coupon coupon = couponRepository.save(couponWallet.getCoupon());
			couponWalletRepository.save(CouponWallet.create(couponWallet.getMemberId(), coupon));
		});

		// When
		List<CouponWallet> actual = couponWalletSearchRepository.findAllByCouponIdAndMemberId(777L, 2L);

		// Then
		assertThat(actual).isEmpty();
	}

	@DisplayName("ID가 3인 회원은 쿠폰 3개를 가지고 있다. - List<CouponWallet>")
	@MethodSource("com.moabam.support.fixture.CouponWalletFixture#provideCouponWalletAll")
	@ParameterizedTest
	void findAllByCouponIdAndMemberId_Id3_success(List<CouponWallet> couponWallets) {
		// Given
		couponWallets.forEach(couponWallet -> {
			Coupon coupon = couponRepository.save(couponWallet.getCoupon());
			couponWalletRepository.save(CouponWallet.create(couponWallet.getMemberId(), coupon));
		});

		// When
		List<CouponWallet> actual = couponWalletSearchRepository.findAllByCouponIdAndMemberId(null, 3L);

		// Then
		assertThat(actual).hasSize(3);
	}

	@DisplayName("회원의 특정 쿠폰 지갑을 성공적으로 조회한다. - CouponWallet")
	@Test
	void findByIdAndMemberId_success() {
		// given
		Coupon coupon = couponRepository.save(discount1000Coupon());
		CouponWallet couponWallet = couponWalletRepository.save(CouponWallet.create(1L, coupon));

		// when
		CouponWallet actual = couponWalletSearchRepository.findByIdAndMemberId(couponWallet.getId(), 1L)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_WALLET));

		// then
		assertThat(actual.getCoupon()).isEqualTo(coupon);
	}

	@DisplayName("특정 회원의 특정 쿠폰을 조회한다. - CouponWallet")
	@Test
	void findByMemberIdAndCouponId_success() {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon());
		couponWalletRepository.save(CouponWallet.create(1L, coupon));

		// When
		CouponWallet actual = couponWalletSearchRepository.findByMemberIdAndCouponId(1L, coupon.getId())
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_WALLET));

		// Then
		assertThat(actual.getCoupon()).isEqualTo(coupon);
	}

	@DisplayName("특정 회원의 특정 쿠폰이 조회되지 않는다. - CouponWallet")
	@Test
	void findByMemberIdAndCouponId_notFound() {
		// When
		Optional<CouponWallet> actual = couponWalletSearchRepository.findByMemberIdAndCouponId(1L, 1L);

		// Then
		assertThat(actual).isEmpty();
	}
}
