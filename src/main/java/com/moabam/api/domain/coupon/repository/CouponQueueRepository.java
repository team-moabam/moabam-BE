package com.moabam.api.domain.coupon.repository;

import static java.util.Objects.*;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponQueueRepository {

	private final ZSetRedisRepository zSetRedisRepository;

	public void addQueue(String couponName, String memberNickname, double score) {
		zSetRedisRepository.addIfAbsent(requireNonNull(couponName), requireNonNull(memberNickname), score);
	}

	public Long queueSize(String couponName) {
		return zSetRedisRepository.size(requireNonNull(couponName));
	}
}
