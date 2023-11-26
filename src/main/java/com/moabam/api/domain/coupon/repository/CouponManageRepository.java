package com.moabam.api.domain.coupon.repository;

import static java.util.Objects.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.StringRedisRepository;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponManageRepository {

	private static final String STOCK_KEY = "%s_INCR";

	private final ZSetRedisRepository zSetRedisRepository;
	private final StringRedisRepository stringRedisRepository;

	public void addIfAbsentQueue(String couponName, Long memberId, double registerTime) {
		zSetRedisRepository.addIfAbsent(requireNonNull(couponName), requireNonNull(memberId), registerTime);
	}

	public Set<Long> popMinQueue(String couponName, long count) {
		return zSetRedisRepository
			.popMin(requireNonNull(couponName), count)
			.stream()
			.map(tuple -> (Long)tuple.getValue())
			.collect(Collectors.toSet());
	}

	public void deleteQueue(String couponName) {
		stringRedisRepository.delete(requireNonNull(couponName));
	}

	public int increaseIssuedStock(String couponName) {
		String stockKey = String.format(STOCK_KEY, requireNonNull(couponName));

		return stringRedisRepository
			.increment(requireNonNull(stockKey))
			.intValue();
	}

	public int getIssuedStock(String couponName) {
		String stockKey = String.format(STOCK_KEY, requireNonNull(couponName));
		String stockValue = stringRedisRepository.get(requireNonNull(stockKey));

		if (stockValue == null) {
			return 0;
		}

		return Integer.parseInt(stockValue);
	}

	public void deleteIssuedStock(String couponName) {
		String stockKey = String.format(STOCK_KEY, requireNonNull(couponName));
		stringRedisRepository.delete(requireNonNull(stockKey));
	}
}
