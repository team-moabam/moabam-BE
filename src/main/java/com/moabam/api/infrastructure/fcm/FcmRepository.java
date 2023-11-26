package com.moabam.api.infrastructure.fcm;

import static java.util.Objects.*;

import java.time.Duration;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FcmRepository {

	private static final long EXPIRE_FCM_TOKEN = 60;

	private final ValueRedisRepository valueRedisRepository;

	public void saveToken(Long memberId, String fcmToken) {
		valueRedisRepository.save(
			String.valueOf(requireNonNull(memberId)),
			requireNonNull(fcmToken),
			Duration.ofDays(EXPIRE_FCM_TOKEN)
		);
	}

	public void deleteTokenByMemberId(Long memberId) {
		valueRedisRepository.delete(String.valueOf(requireNonNull(memberId)));
	}

	public String findTokenByMemberId(Long memberId) {
		return valueRedisRepository.get(String.valueOf(requireNonNull(memberId)));
	}

	public boolean existsTokenByMemberId(Long memberId) {
		return valueRedisRepository.hasKey(String.valueOf(requireNonNull(memberId)));
	}
}
