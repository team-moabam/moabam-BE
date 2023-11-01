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
	private static final long EXPIRE_FCM_TOKEN = 60;
	private static final String TO = "_TO_";
	private final StringRedisRepository stringRedisRepository;

	// TODO : 세연님 로그인 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 저장하면 됩니다.
	public void saveFcmToken(Long key, String value) {
		stringRedisRepository.save(
			String.valueOf(requireNonNull(key)),
			requireNonNull(value),
			requireNonNull(Duration.ofDays(EXPIRE_FCM_TOKEN))
		);
	}

	public void saveKnockNotification(Long memberId, Long targetId) {
		stringRedisRepository.save(
			requireNonNull(memberId) + TO + requireNonNull(targetId),
			"",
			requireNonNull(Duration.ofMinutes(EXPIRE_KNOCK))
		);
	}

	// TODO : 세연님 로그아웃 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 삭제하시면 됩니다.
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
