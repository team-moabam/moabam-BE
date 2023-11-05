package com.moabam.api.domain.entity;

import com.moabam.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "daily_member_certification") // 매일 사용자가 방에 인증을 완료했는지 -> createdAt으로 인증 시각 확인
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyMemberCertification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", nullable = false, updatable = false)
	private Long memberId;

	@Column(name = "room_id", nullable = false, updatable = false)
	private Long roomId;
}
