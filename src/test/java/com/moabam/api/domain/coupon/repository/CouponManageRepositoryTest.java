package com.moabam.api.domain.coupon.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.infrastructure.redis.ValueRedisRepository;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

@ExtendWith(MockitoExtension.class)
class CouponManageRepositoryTest {

	@InjectMocks
	CouponManageRepository couponManageRepository;

	@Mock
	ZSetRedisRepository zSetRedisRepository;

	@Mock
	ValueRedisRepository valueRedisRepository;

	@DisplayName("쿠폰 대기열에 사용자가 성공적으로 등록된다. - Void")
	@Test
	void addIfAbsentQueue_success() {
		// When
		couponManageRepository.addIfAbsentQueue("couponName", 1L, 1);

		// Then
		verify(zSetRedisRepository).addIfAbsent(any(String.class), any(Long.class), any(double.class), any(int.class));
	}

	@DisplayName("쿠폰명이 Null인 대기열에 사용자를 등록한다.- NullPointerException")
	@Test
	void addIfAbsentQueue_couponName_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.addIfAbsentQueue(null, 1L, 1))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰 대기열에 사용자 ID가 Null인 사용자를 등록한다. - NullPointerException")
	@Test
	void addIfAbsentQueue_memberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.addIfAbsentQueue("couponName", null, 1))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰 대기열에서 성공적으로 10명을 조회한다. - Set<Long>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideValues_Object")
	@ParameterizedTest
	void range_success(Set<Object> values) {
		// Given
		given(zSetRedisRepository.range(any(String.class), any(long.class), any(long.class))).willReturn(values);

		// When
		Set<Long> actual = couponManageRepository.rangeQueue("couponName", 0, 10);

		// Then
		assertThat(actual).hasSize(10);
	}

	@DisplayName("쿠폰명이 Null인 대기열에서 사용자를 조회한다.  - NullPointerException")
	@Test
	void range_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.rangeQueue(null, 0, 10))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰 대기열을 성공적으로 삭제한다. - Void")
	@Test
	void deleteQueue_success() {
		// When
		couponManageRepository.deleteQueue("couponName");

		// Then
		verify(valueRedisRepository).delete(any(String.class));
	}

	@DisplayName("쿠폰명이 Null인 대기열을 삭제한다. - NullPointerException")
	@Test
	void deleteQueue_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.deleteQueue(null))
			.isInstanceOf(NullPointerException.class);
	}
}
