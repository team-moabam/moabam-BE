package com.moabam.api.domain.member;

import static com.moabam.global.common.util.GlobalConstant.*;
import static com.moabam.global.common.util.RandomUtils.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;

import com.moabam.api.domain.bug.Bug;
import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.common.util.BaseImageUrl;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

@Entity
@Getter
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET deleted_at = CURRENT_TIMESTAMP where id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "social_id", nullable = false, unique = true)
	private String socialId;

	@Column(name = "nickname", nullable = false, unique = true)
	private String nickname;

	@Column(name = "intro", length = 30)
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

	@Embedded
	private Bug bug;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	@ColumnDefault("'USER'")
	private Role role;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private Member(Long id, String socialId, Bug bug) {
		this.id = id;
		this.socialId = requireNonNull(socialId);
		this.nickname = createNickName();
		this.profileImage = BaseImageUrl.PROFILE_URL;
		this.bug = requireNonNull(bug);
		this.role = Role.USER;
	}

	public void enterMorningRoom() {
		currentMorningCount++;
	}

	public void enterNightRoom() {
		currentNightCount++;
	}

	public void exitMorningRoom() {
		if (currentMorningCount > 0) {
			currentMorningCount--;
		}
	}

	public void exitNightRoom() {
		if (currentNightCount > 0) {
			currentNightCount--;
		}
	}

	public int getLevel() {
		return (int)(totalCertifyCount / LEVEL_DIVISOR) + 1;
	}

	public void increaseTotalCertifyCount() {
		this.totalCertifyCount++;
	}

	public void delete(LocalDateTime now) {
		socialId = deleteSocialId(now);
	}

	public void changeNickName(String nickname) {
		this.nickname = requireNonNull(nickname, this.nickname);
	}

	public void changeIntro(String intro) {
		this.intro = requireNonNull(intro, this.intro);
	}

	public void changeProfileUri(String newProfileUri) {
		this.profileImage = requireNonNullElse(newProfileUri, profileImage);
	}

	private String createNickName() {
		return "오목눈이#" + randomStringValues();
	}

	private String deleteSocialId(LocalDateTime now) {
		return "delete_" + now.toString() + randomNumberValues();
	}
}
