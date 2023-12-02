package com.moabam.api.domain.room.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.DailyMemberCertification;

public interface DailyMemberCertificationRepository extends JpaRepository<DailyMemberCertification, Long> {

	boolean existsByMemberIdAndRoomIdAndCreatedAtBetween(Long memberId, Long roomId, LocalDateTime startTime,
		LocalDateTime endTime);

	boolean existsByRoomIdAndCreatedAtBetween(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
}
