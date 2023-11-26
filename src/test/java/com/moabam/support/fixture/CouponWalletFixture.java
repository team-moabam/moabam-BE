package com.moabam.support.fixture;

import static com.moabam.support.fixture.CouponFixture.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.moabam.api.domain.coupon.CouponWallet;

public final class CouponWalletFixture {

	public static Stream<Arguments> provideCouponWalletByCouponId1_total5() {
		return Stream.of(Arguments.of(
			List.of(
				CouponWallet.create(1L, coupon("c1", 1)),
				CouponWallet.create(1L, coupon("c2", 2)),
				CouponWallet.create(1L, coupon("c3", 3)),
				CouponWallet.create(1L, coupon("c4", 4)),
				CouponWallet.create(1L, coupon("c5", 5))
			))
		);
	}

	public static Stream<Arguments> provideCouponWalletAll() {
		return Stream.of(Arguments.of(
			List.of(
				CouponWallet.create(1L, coupon("c2", 2)),
				CouponWallet.create(2L, coupon("c3", 3)),
				CouponWallet.create(2L, coupon("c4", 4)),
				CouponWallet.create(3L, coupon("c5", 5)),
				CouponWallet.create(3L, coupon("c6", 6)),
				CouponWallet.create(3L, coupon("c7", 7))
			))
		);
	}
}
