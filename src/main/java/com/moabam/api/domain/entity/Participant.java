package com.moabam.api.domain.entity;

import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "participant")
@SQLDelete(sql = "UPDATE participant SET deleted_at = CURRENT_TIMESTAMP where id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", updatable = false, nullable = false)
	private Room room;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@Column(name = "is_manager")
	private boolean isManager;

	@Column(name = "certify_count")
	private int certifyCount;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private Participant(Long id, Room room, Long memberId) {
		this.id = id;
		this.room = requireNonNull(room);
		this.memberId = requireNonNull(memberId);
		this.isManager = false;
		this.certifyCount = 0;
	}

	public void disableManager() {
		this.isManager = false;
	}

	public void enableManager() {
		this.isManager = true;
	}

	public void updateCertifyCount() {
		this.certifyCount += 1;
	}
}
