package com.moabam.api.domain.coupon.repository;

import static com.moabam.support.fixture.CouponFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.support.annotation.QuerydslRepositoryTest;

@QuerydslRepositoryTest
class CouponWalletSearchRepositoryTest {

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponWalletRepository couponWalletRepository;

	@Autowired
	private CouponWalletSearchRepository couponWalletSearchRepository;

	@DisplayName("회원의 특정 쿠폰 지갑을 조회한다.")
	@Test
	void find_by_id_and_member_id() {
		// given
		Long id = 1L;
		Long memberId = 1L;
		Coupon coupon = couponRepository.save(discount1000Coupon());
		couponWalletRepository.save(couponWallet(memberId, coupon));

		// when
		CouponWallet actual = couponWalletSearchRepository.findByIdAndMemberId(id, memberId).orElseThrow();

		// then
		assertThat(actual.getCoupon()).isEqualTo(coupon);
	}
}
