package com.moabam.api.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponSearchRepository;
import com.moabam.api.dto.coupon.CouponSearchRequest;
import com.moabam.global.config.JpaConfig;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.CouponFixture;

@DataJpaTest
@Import({JpaConfig.class, CouponSearchRepository.class})
class CouponSearchRepositoryTest {

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private CouponSearchRepository couponSearchRepository;

	@DisplayName("특정 쿠폰을 조회한다. - CouponResponse")
	@Test
	void couponSearchRepository_findById() {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon(10, 100));

		// When
		Coupon actual = couponSearchRepository.findById(coupon.getId())
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));

		// Then
		assertThat(actual.getStock()).isEqualTo(coupon.getStock());
		assertThat(actual.getPoint()).isEqualTo(coupon.getPoint());
	}

	@DisplayName("존재하지 않는 쿠폰을 조회한다. - NotFoundException")
	@Test
	void couponSearchRepository_findById_NotFoundException() {
		// When
		Optional<Coupon> actual = couponSearchRepository.findById(77777L);

		// Then
		assertThat(actual).isEmpty();
	}

	@DisplayName("모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, true, true);
		LocalDateTime now = LocalDateTime.now();

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(coupons.size());
	}

	@DisplayName("시작 전이거나 진행 중인 쿠폰들을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_and_ongoing_notStarted(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, true, false);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(8);
	}

	@DisplayName("종료 됐거나 진행 중인 쿠폰들을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_and_ongoing_ended(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, false, true);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(5);
	}

	@DisplayName("진행 중이 아니고, 시작 전이거나, 종료된 쿠폰들을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_ongoing_and_ended(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(false, true, true);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(7);
	}

	@DisplayName("진행 중인 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_ongoing(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, false, false);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(3);
	}

	@DisplayName("시작 적인 쿠폰들을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_notStarted(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(false, true, false);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(5);
	}

	@DisplayName("종료된 쿠폰들을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus_ended(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(false, false, true);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).hasSize(2);
	}

	@DisplayName("상태조건을 걸지 않아서 모든 쿠폰이 조회되지 않는다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponSearchRepository_findAllByStatus__not_status(List<Coupon> coupons) {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(false, false, false);
		LocalDateTime now = LocalDateTime.of(2023, 5, 1, 0, 0);

		couponRepository.saveAll(coupons);

		// When
		List<Coupon> actual = couponSearchRepository.findAllByStatus(now, request);

		// Then
		assertThat(actual).isEmpty();
	}
}
