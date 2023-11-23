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

	private final StringRedisRepository stringRedisRepository;

	public void saveKnock(String key) {
		stringRedisRepository.save(
			requireNonNull(key),
			BLANK,
			Duration.ofHours(EXPIRE_KNOCK)
		);
	}

	public boolean existsKnockByKey(String key) {
		return stringRedisRepository.hasKey(requireNonNull(key));
	}
}
