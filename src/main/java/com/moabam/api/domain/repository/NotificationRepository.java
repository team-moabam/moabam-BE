package com.moabam.api.domain.repository;

import static java.util.Objects.*;

import java.time.Duration;

import org.springframework.stereotype.Repository;

import com.moabam.global.common.repository.StringRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

	private static final long EXPIRE_KNOCK = 20;
	private static final String TO = "_TO_";
	private final StringRedisRepository stringRedisRepository;

	public void save(Long key, String value, Duration expireTime) {
		stringRedisRepository.save(
			String.valueOf(requireNonNull(key)),
			requireNonNull(value),
			requireNonNull(expireTime)
		);
	}

	public void saveKnockNotification(Long memberId, Long targetId) {
		stringRedisRepository.save(
			requireNonNull(memberId) + TO + requireNonNull(targetId),
			"",
			requireNonNull(Duration.ofMinutes(EXPIRE_KNOCK))
		);
	}

	public void deleteFcmTokenByMemberId(Long memberId) {
		stringRedisRepository.delete(String.valueOf(requireNonNull(memberId)));
	}

	public String findFcmTokenByMemberId(Long memberId) {
		return stringRedisRepository.get(String.valueOf(requireNonNull(memberId)));
	}

	public boolean existsFcmTokenByMemberId(Long memberId) {
		return stringRedisRepository.hasKey(String.valueOf(requireNonNull(memberId)));
	}

	public boolean existsKnockByMemberId(Long memberId, Long targetId) {
		return stringRedisRepository.hasKey(requireNonNull(memberId) + TO + requireNonNull(targetId));
	}
}
