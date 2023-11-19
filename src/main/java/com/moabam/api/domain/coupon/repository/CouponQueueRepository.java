package com.moabam.api.domain.coupon.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponQueueRepository {

	private final ZSetRedisRepository zSetRedisRepository;

	public void addQueue(String couponName, String memberNickname, double score) {
		zSetRedisRepository.addIfAbsent(couponName, memberNickname, score);
	}

	public Set<String> getQueue(String couponName, long startRank, long endRank) {
		return zSetRedisRepository.range(couponName, startRank, endRank);
	}

	public Set<String> popQueue(String couponName, long count) {
		return zSetRedisRepository.popMin(couponName, count);
	}

	public void removeQueueByMemberNickname(String couponName, String memberNickname) {
		zSetRedisRepository.remove(couponName, memberNickname);
	}

	public Long queueSize(String couponName) {
		return zSetRedisRepository.size(couponName);
	}
}
