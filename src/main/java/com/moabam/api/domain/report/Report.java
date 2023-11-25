package com.moabam.api.domain.report;

import static java.util.Objects.*;

import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
@Entity
public class Report extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reporter_id", nullable = false, updatable = false)
	private Long reporterId;

	@Column(name = "reported_member_id", nullable = false, updatable = false)
	private Long reportedMemberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", updatable = false)
	private Room room;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_id", updatable = false)
	private Certification certification;

	@Column(name = "description")
	private String description;

	@Builder
	private Report(Long reporterId, Long reportedMemberId, Room room, Certification certification, String description) {
		this.reporterId = requireNonNull(reporterId);
		this.reportedMemberId = requireNonNull(reportedMemberId);
		this.room = room;
		this.certification = certification;
	}
}
