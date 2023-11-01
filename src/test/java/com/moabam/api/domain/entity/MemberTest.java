package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.entity.enums.Role;
import com.moabam.global.common.util.BaseImageUrl;

class MemberTest {

	String socialId = "1";
	String nickname = "밥세공기";
	String profileImage = "kakao/profile/url";

	@DisplayName("회원 생성 성공")
	@Test
	void create_member_success() {
		// When + Then
		assertThatNoException().isThrownBy(() -> Member.builder()
			.socialId(socialId)
			.nickname(nickname)
			.profileImage(profileImage)
			.build());
	}

	@DisplayName("프로필 이미지 없이 회원 생성 성공")
	@Test
	void create_member_noImage_success() {
		// When + Then
		assertThatNoException().isThrownBy(() -> {
			Member member = Member.builder()
				.socialId(socialId)
				.nickname(nickname)
				.profileImage(null)
				.build();

			assertAll(
				() -> assertThat(member.getProfileImage()).isEqualTo(BaseImageUrl.PROFILE_URL),
				() -> assertThat(member.getRole()).isEqualTo(Role.USER),
				() -> assertThat(member.getBug().getNightBug()).isZero(),
				() -> assertThat(member.getBug().getGoldenBug()).isZero(),
				() -> assertThat(member.getBug().getMorningBug()).isZero(),
				() -> assertThat(member.getTotalCertifyCount()).isZero(),
				() -> assertThat(member.getReportCount()).isZero(),
				() -> assertThat(member.getCurrentMorningCount()).isZero(),
				() -> assertThat(member.getCurrentNightCount()).isZero()
			);
		});
	}

	@DisplayName("소셜ID에 따른 회원 생성 실패")
	@Test
	void creat_member_failBy_socialId() {
		// When + Then
		assertThatThrownBy(Member.builder()
			.nickname(nickname)::build)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("닉네임에 따른 회원 생성 실패")
	@Test
	void create_member_failBy_nickname() {
		// When + Then
		assertThatThrownBy(Member.builder()
			.socialId(socialId)::build)
			.isInstanceOf(NullPointerException.class);
	}
}
