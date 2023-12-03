package com.moabam.api.domain.room;

import static java.util.Objects.*;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "daily_room_certification") // 매일 방이 인증을 완료했는지 -> certifiedAt으로 인증 날짜 확인
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRoomCertification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "room_id", nullable = false, updatable = false)
	private Long roomId;

	@Column(name = "certified_at", nullable = false, updatable = false)
	private LocalDate certifiedAt;

	@Builder
	private DailyRoomCertification(Long id, Long roomId, LocalDate certifiedAt) {
		this.id = id;
		this.roomId = requireNonNull(roomId);
		this.certifiedAt = requireNonNull(certifiedAt);
	}
}
