package com.moabam.admin.domain.admin;

import static com.moabam.global.common.util.RandomUtils.*;
import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.member.Role;
import com.moabam.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nickname", unique = true)
	private String nickname;

	@Column(name = "social_id", nullable = false, unique = true)
	private String socialId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	@ColumnDefault("'USER'")
	private Role role;

	@Builder
	private Admin(String socialId) {
		this.socialId = requireNonNull(socialId);
		this.nickname = createNickName();
		this.role = Role.ADMIN;
	}

	private String createNickName() {
		return "오목눈이#" + randomStringValues();
	}
}
