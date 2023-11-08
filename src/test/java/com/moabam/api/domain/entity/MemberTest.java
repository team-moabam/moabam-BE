package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.entity.enums.Role;
import com.moabam.global.common.util.BaseImageUrl;
import com.moabam.support.fixture.MemberFixture;

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
			.bug(Bug.builder().build())
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
				.bug(Bug.builder().build())
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

	@DisplayName("멤버 방 출입 기능 테스트")
	@Nested
	class MemberRoomInOut {

		@DisplayName("회원 방 입장 성공")
		@Test
		void member_room_enter_success() {
			// given
			Member member = MemberFixture.member();

			// when
			int beforeMorningCount = member.getCurrentMorningCount();
			member.enterMorningRoom();

			int beforeNightCount = member.getCurrentNightCount();
			member.enterNightRoom();

			// then
			assertThat(member.getCurrentMorningCount()).isEqualTo(beforeMorningCount + 1);
			assertThat(member.getCurrentMorningCount()).isEqualTo(beforeNightCount + 1);
		}

		@DisplayName("회원 방 탈출 성공")
		@Test
		void member_room_exit_success() {
			// given
			Member member = MemberFixture.member();

			// when
			member.exitMorningRoom();
			member.exitNightRoom();

			// then
			assertThat(member.getCurrentMorningCount()).isZero();
			assertThat(member.getCurrentMorningCount()).isZero();
		}
	}
}
