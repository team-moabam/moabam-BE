package com.moabam.api.domain.coupon.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

@ExtendWith(MockitoExtension.class)
class CouponQueueRepositoryTest {

	@InjectMocks
	private CouponQueueRepository couponQueueRepository;

	@Mock
	private ZSetRedisRepository zSetRedisRepository;

	@DisplayName("특정 쿠폰의 대기열에 사용자가 성공적으로 등록된다. - Void")
	@Test
	void addIfAbsent() {
		// When
		couponQueueRepository.addIfAbsent("couponName", 1L, 1);

		// Then
		verify(zSetRedisRepository).addIfAbsent(any(String.class), any(Long.class), any(Double.class));
	}

	@DisplayName("특정 쿠폰의 대기열에 사용자 등록 시, 필요한 값이 NULL 이다.- NullPointerException")
	@Test
	void addIfAbsent_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponQueueRepository.addIfAbsent(null, 1L, 1))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("특정 쿠폰을 발급한 사용자가 3명이다. - Long")
	@Test
	void size() {
		// Given
		given(zSetRedisRepository.size(any(String.class))).willReturn(3L);

		// When
		long actual = couponQueueRepository.size("key");

		// Then
		assertThat(actual).isEqualTo(3);
	}

	@DisplayName("특정 쿠폰을 발급한 사용자 수 조회 시, 필요한 값이 Null이다. - NullPointerException")
	@Test
	void size_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponQueueRepository.size(null))
			.isInstanceOf(NullPointerException.class);
	}
}
