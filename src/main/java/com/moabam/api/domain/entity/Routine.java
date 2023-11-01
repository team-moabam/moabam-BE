package com.moabam.api.domain.entity;

import static java.util.Objects.*;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "routine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routine extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false, updatable = false)
	private Room room;

	@Column(name = "content", nullable = false, length = 60)
	private String content;

	@Builder
	private Routine(Long id, Room room, String content) {
		this.id = id;
		this.room = requireNonNull(room);
		this.content = requireNonNull(content);
	}

	public void changeContent(String content) {
		this.content = content;
	}
}
