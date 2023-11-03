package com.moabam.api.domain.repository;

import static com.moabam.global.common.util.GlobalConstant.*;
import static java.util.Objects.*;

import java.time.Duration;

import org.springframework.stereotype.Repository;

import com.moabam.global.common.repository.StringRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

	private final StringRedisRepository stringRedisRepository;

	// TODO : 세연님 로그인 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 저장하면 됩니다.
	public void saveFcmToken(Long key, String value) {
		stringRedisRepository.save(
			String.valueOf(requireNonNull(key)),
			requireNonNull(value),
			requireNonNull(Duration.ofDays(EXPIRE_FCM_TOKEN))
		);
	}

	public void saveKnockNotification(Long memberId, Long targetId, Long roomId) {
		String key = requireNonNull(roomId) + UNDER_BAR + requireNonNull(memberId) + TO + requireNonNull(targetId);
		stringRedisRepository.save(key, BLANK, requireNonNull(Duration.ofHours(EXPIRE_KNOCK)));
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

	public boolean existsKnockByMemberId(Long memberId, Long targetId, Long roomId) {
		String key = requireNonNull(roomId) + UNDER_BAR + requireNonNull(memberId) + TO + requireNonNull(targetId);

		return stringRedisRepository.hasKey(key);
	}
}
