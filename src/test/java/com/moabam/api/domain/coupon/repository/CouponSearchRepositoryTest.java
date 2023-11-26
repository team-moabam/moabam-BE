package com.moabam.api.domain.coupon.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.global.config.JpaConfig;
import com.moabam.support.fixture.CouponFixture;

@DataJpaTest
@Import({JpaConfig.class, CouponSearchRepository.class})
class CouponSearchRepositoryTest {

	@Autowired
	CouponRepository couponRepository;

	@Autowired
	CouponSearchRepository couponSearchRepository;

	@DisplayName("발급 가능한 쿠폰을 성공적으로 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void findAllByStatus_success(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, false);
		LocalDate now = LocalDate.of(2023, 7, 1);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getStartAt()).isEqualTo(LocalDate.of(2023, 7, 1));
	}

	@DisplayName("모든 쿠폰을 발급 가능 날짜 순으로 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void findAllByStatus_order_by_startAt(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(true, true);
		LocalDate now = LocalDate.now();

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(coupons.size());
		assertThat(actual.get(0).getStartAt()).isEqualTo(LocalDate.of(2023, 3, 1));
	}

	@DisplayName("발급 가능한 쿠폰 포함하여 쿠폰 정보 오픈 중인 쿠폰들을 발급 가능 날짜 순으로 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void findAllByStatus_opened_order_by_startAt(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(true, false);
		LocalDate now = LocalDate.of(2023, 7, 1);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(3);
		assertThat(actual.get(0).getStartAt()).isEqualTo(LocalDate.of(2023, 7, 1));
	}

	@DisplayName("종료된 쿠폰들을 발급 가능 날짜 순으로 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void findAllByStatus_ended_order_by_startAt(List<Coupon> coupons) {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, true);
		LocalDate now = LocalDate.of(2023, 8, 1);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(5);
		assertThat(actual.get(0).getStartAt()).isEqualTo(LocalDate.of(2023, 3, 1));
	}
}
