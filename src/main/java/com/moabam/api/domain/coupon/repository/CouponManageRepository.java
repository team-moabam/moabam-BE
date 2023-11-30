package com.moabam.api.domain.coupon.repository;

import static java.util.Objects.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.ValueRedisRepository;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponManageRepository {

	private static final int EXPIRE_DAYS = 2;

	private final ZSetRedisRepository zSetRedisRepository;
	private final ValueRedisRepository valueRedisRepository;

	public void addIfAbsentQueue(String couponName, Long memberId, double registerTime) {
		zSetRedisRepository.addIfAbsent(
			requireNonNull(couponName),
			requireNonNull(memberId),
			registerTime,
			EXPIRE_DAYS
		);
	}

	public Set<Long> rangeQueue(String couponName, long start, long end) {

		return zSetRedisRepository
			.range(requireNonNull(couponName), start, end)
			.stream()
			.map(memberId -> Long.parseLong(String.valueOf(memberId)))
			.collect(Collectors.toSet());
	}

	public int rankQueue(String couponName, Long memberId) {
		return zSetRedisRepository
			.rank(requireNonNull(couponName), requireNonNull(memberId))
			.intValue();
	}

	public void deleteQueue(String couponName) {
		valueRedisRepository.delete(requireNonNull(couponName));
	}
}
