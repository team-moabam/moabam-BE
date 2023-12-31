package com.moabam.api.domain.room;

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
@Table(name = "certification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routine_id", nullable = false, updatable = false)
	private Routine routine;

	@Column(name = "member_id", nullable = false, updatable = false)
	private Long memberId;

	@Column(name = "image", nullable = false)
	private String image;

	@Builder
	private Certification(Long id, Routine routine, Long memberId, String image) {
		this.id = id;
		this.routine = requireNonNull(routine);
		this.memberId = requireNonNull(memberId);
		this.image = requireNonNull(image);
	}

	public void changeImage(String image) {
		this.image = image;
	}
}
