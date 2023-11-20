package com.moabam.api.domain.notification.repository;

import static com.moabam.global.common.util.GlobalConstant.*;
import static java.util.Objects.*;

import java.time.Duration;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.StringRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

	private static final long EXPIRE_KNOCK = 12;
	private static final long EXPIRE_FCM_TOKEN = 60;

	private final StringRedisRepository stringRedisRepository;

	// TODO : 세연님 로그인 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 저장하면 됩니다. Front와 상의 후 삭제예정
	public void saveFcmToken(Long memberId, String fcmToken) {
		stringRedisRepository.save(
			String.valueOf(requireNonNull(memberId)),
			requireNonNull(fcmToken),
			Duration.ofDays(EXPIRE_FCM_TOKEN)
		);
	}

	public void saveKnockNotification(String knockKey) {
		stringRedisRepository.save(
			requireNonNull(knockKey),
			BLANK,
			Duration.ofHours(EXPIRE_KNOCK)
		);
	}

	// TODO : 세연님 로그아웃 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 삭제하시면 됩니다.
	public void deleteFcmTokenByMemberId(Long memberId) {
		stringRedisRepository.delete(String.valueOf(requireNonNull(memberId)));
	}

	public String findFcmTokenByMemberId(Long memberId) {
		return stringRedisRepository.get(String.valueOf(requireNonNull(memberId)));
	}

	public boolean existsByKey(String key) {
		return stringRedisRepository.hasKey(requireNonNull(key));
	}

	public boolean existsFcmTokenByMemberId(Long memberId) {
		return stringRedisRepository.hasKey(String.valueOf(requireNonNull(memberId)));
	}
}
