package com.moabam.api.domain;

import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.common.util.BaseImageUrl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(name = "social_id", nullable = false, unique = true)
	private String socialId;

	@Column(name = "nickname", nullable = false, unique = true)
	private String nickname;

	@Column(name = "intro")
	private String intro;

	@Column(name = "profile_image", nullable = false)
	private String profileImage;

	@Column(name = "total_certify_count", nullable = false)
	@ColumnDefault("0")
	private long totalCertifyCount;

	@Column(name = "report_count", nullable = false)
	@ColumnDefault("0")
	private int reportCount;

	@Column(name = "current_night_count", nullable = false)
	@ColumnDefault("0")
	private int currentNightCount;

	@Column(name = "current_morning_count", nullable = false)
	@ColumnDefault("0")
	private int currentMorningCount;

	@Column(name = "morning_bug", nullable = false)
	@ColumnDefault("0")
	private int morningBug;

	@Column(name = "night_bug", nullable = false)
	@ColumnDefault("0")
	private int nightBug;

	@Column(name = "golden_bug", nullable = false)
	@ColumnDefault("0")
	private int goldenBug;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private Member(String socialId, String nickname, String profileImage) {
		this.socialId = requireNonNull(socialId);
		this.nickname = requireNonNull(nickname);
		this.profileImage = requireNonNullElse(profileImage, BaseImageUrl.PROFILE_URL.getUrl());
		this.role = Role.USER;
	}
}
