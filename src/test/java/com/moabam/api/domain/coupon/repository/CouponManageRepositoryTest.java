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
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.moabam.api.infrastructure.redis.StringRedisRepository;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

@ExtendWith(MockitoExtension.class)
class CouponManageRepositoryTest {

	@InjectMocks
	private CouponManageRepository couponManageRepository;

	@Mock
	private ZSetRedisRepository zSetRedisRepository;

	@Mock
	private StringRedisRepository stringRedisRepository;

	@DisplayName("쿠폰 대기열에 사용자가 성공적으로 등록된다. - Void")
	@Test
	void addIfAbsentQueue_success() {
		// When
		couponManageRepository.addIfAbsentQueue("couponName", 1L, 1);

		// Then
		verify(zSetRedisRepository).addIfAbsent(any(String.class), any(Long.class), any(double.class));
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

	@DisplayName("쿠폰 대기열에서 10명을 꺼내고 삭제한다.")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideTypedTuples")
	@ParameterizedTest
	void popMinQueue_success(Set<TypedTuple<Object>> tuples) {
		// Given
		given(zSetRedisRepository.popMin(any(String.class), any(long.class))).willReturn(tuples);

		// When
		Set<Long> actual = couponManageRepository.popMinQueue("couponName", 10);

		// Then
		assertThat(actual).hasSize(10);
	}

	@DisplayName("쿠폰명이 Null인 대기열에서 사용자를 꺼낸다.  - NullPointerException")
	@Test
	void popMinQueue_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.popMinQueue(null, 10))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰 대기열을 성공적으로 삭제한다. - Void")
	@Test
	void deleteQueue_success() {
		// When
		couponManageRepository.deleteQueue("couponName");

		// Then
		verify(stringRedisRepository).delete(any(String.class));
	}

	@DisplayName("쿠폰명이 Null인 대기열을 삭제한다. - NullPointerException")
	@Test
	void deleteQueue_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.deleteQueue(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰의 할당된 재고를 성공적으로 증가시킨다. - int")
	@Test
	void increaseIssuedStock_success() {
		// Given
		given(stringRedisRepository.increment(any(String.class))).willReturn(77L);

		// When
		int actual = couponManageRepository.increaseIssuedStock("couponName");

		// Then
		assertThat(actual).isEqualTo(77);
	}

	@DisplayName("쿠폰명이 Null인 쿠폰의 할당된 재고를 증가시킨다. - NullPointerException")
	@Test
	void increaseIssuedStock_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.increaseIssuedStock(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("쿠폰의 현재 할당된 재고를 성공적으로 조회한다. - int")
	@Test
	void getIssuedStock_success() {
		// Given
		given(stringRedisRepository.get(any(String.class))).willReturn("1");

		// When
		int actual = couponManageRepository.getIssuedStock("couponName");

		// Then
		assertThat(actual).isEqualTo(1);
	}

	@DisplayName("쿠폰의 현재 할당된 재고가 없어서 0이 조회된다. - int")
	@Test
	void getIssuedStock_zero() {
		// Given
		given(stringRedisRepository.get(any(String.class))).willReturn(null);

		// When
		int actual = couponManageRepository.getIssuedStock("couponName");

		// Then
		assertThat(actual).isZero();
	}

	@DisplayName("쿠폰명이 Null인 쿠폰의 할당된 재고를 조회한다. - NullPointerException")
	@Test
	void getIssuedStock_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.getIssuedStock(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("할당된 쿠폰 재고를 성공적으로 삭제한다. - Void")
	@Test
	void deleteIssuedStock_success() {
		// When
		couponManageRepository.deleteIssuedStock("couponName");

		// Then
		verify(stringRedisRepository).delete(any(String.class));
	}

	@DisplayName("쿠폰명이 Null인 할당된 쿠폰 재고를 삭제한다. - NullPointerException")
	@Test
	void deleteIssuedStock_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> couponManageRepository.deleteIssuedStock(null))
			.isInstanceOf(NullPointerException.class);
	}
}
