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

	private static final String KNOCK_KEY = "roomId=%s_targetId=%s_memberId=%s";
	private static final long EXPIRE_KNOCK = 12;

	private final ValueRedisRepository valueRedisRepository;

	public void saveKnock(Long roomId, Long targetId, Long memberId) {
		String knockKey = String.format(
			KNOCK_KEY,
			requireNonNull(roomId),
			requireNonNull(targetId),
			requireNonNull(memberId));

		valueRedisRepository.save(knockKey, BLANK, Duration.ofHours(EXPIRE_KNOCK));
	}

	public boolean existsKnockByKey(Long roomId, Long targetId, Long memberId) {
		String knockKey = String.format(
			KNOCK_KEY,
			requireNonNull(roomId),
			requireNonNull(targetId),
			requireNonNull(memberId));

		return valueRedisRepository.hasKey(requireNonNull(knockKey));
	}
}
