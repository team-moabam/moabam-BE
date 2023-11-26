package com.moabam.api.domain.notification.repository;

import static com.moabam.global.common.util.GlobalConstant.*;
import static java.util.Objects.*;

import java.time.Duration;

import org.springframework.stereotype.Repository;

import com.moabam.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

	private static final String KNOCK_KEY = "room_%s_member_%s_knocks_%s";
	private static final long EXPIRE_KNOCK = 12;

	private final ValueRedisRepository valueRedisRepository;

	public void saveKnock(Long memberId, Long targetId, Long roomId) {
		String knockKey =
			String.format(KNOCK_KEY, requireNonNull(roomId), requireNonNull(memberId), requireNonNull(targetId));

		valueRedisRepository.save(knockKey, BLANK, Duration.ofHours(EXPIRE_KNOCK));
	}

	public boolean existsKnockByKey(Long memberId, Long targetId, Long roomId) {
		String knockKey =
			String.format(KNOCK_KEY, requireNonNull(roomId), requireNonNull(memberId), requireNonNull(targetId));

		return valueRedisRepository.hasKey(requireNonNull(knockKey));
	}
}
