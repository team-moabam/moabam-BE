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
@Table(name = "certifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routine_id", nullable = false)
	private Routine routine;

	@Column(name = "member_id", nullable = false, updatable = false)
	private Long memberId;

	@Column(name = "image", nullable = false)
	private String image;

	@Builder
	private Certification(Routine routine, Long memberId, String image) {
		this.routine = requireNonNull(routine);
		this.memberId = requireNonNull(memberId);
		this.image = requireNonNull(image);
	}

	public void changeImage(String image) {
		this.image = image;
	}
}
