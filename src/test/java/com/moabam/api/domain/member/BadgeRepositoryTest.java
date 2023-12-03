package com.moabam.api.domain.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.application.member.BadgeService;
import com.moabam.api.domain.member.repository.BadgeRepository;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.MemberFixture;

@QuerydslRepositoryTest
class BadgeRepositoryTest {

	@Autowired
	BadgeRepository badgeRepository;

	@Autowired
	MemberRepository memberRepository;

	@DisplayName("인증 횟수에 따른 값 뱃지 확인")
	@Test
	void get_badge_by_certifyCount() {
		assertThat(BadgeType.getBadgeFrom(10).get()).isEqualTo(BadgeType.BIRTH);
		assertThat(BadgeType.getBadgeFrom(100).get()).isEqualTo(BadgeType.LEVEL10);
		assertThat(BadgeType.getBadgeFrom(500).get()).isEqualTo(BadgeType.LEVEL50);
		assertThat(BadgeType.getBadgeFrom(9)).isEmpty();
	}

	@DisplayName("뱃지 생성 성공")
	@ParameterizedTest
	@ValueSource(ints = {10, 100, 500})
	void member_get_badge_success(int certifyCount) {
		// given
		BadgeService badgeService = new BadgeService(badgeRepository);

		Member member = MemberFixture.member();
		for (int i = 0; i < certifyCount; i++) {
			member.increaseTotalCertifyCount();
		}

		memberRepository.save(member);

		// when
		badgeService.createBadge(member.getId(), member.getTotalCertifyCount());
		BadgeType expectedType = BadgeType.getBadgeFrom(certifyCount).get();

		// then
		assertThat(badgeRepository.existsByMemberIdAndType(member.getId(), expectedType))
			.isTrue();
	}

	@DisplayName("뱃지가 있으면 저장하지 않는다.")
	@ParameterizedTest
	@ValueSource(ints = {10, 100, 500})
	void already_exist_bage_then_no_save(int certifyCount) {
		// given
		BadgeService badgeService = new BadgeService(badgeRepository);

		Member member = MemberFixture.member();
		for (int i = 0; i < certifyCount; i++) {
			member.increaseTotalCertifyCount();
		}

		memberRepository.save(member);

		// when
		BadgeType expectedType = BadgeType.getBadgeFrom(certifyCount).get();

		Badge badge = Badge.builder().memberId(member.getId()).type(expectedType).build();
		badgeRepository.save(badge);

		// then
		assertThatNoException()
			.isThrownBy(() -> badgeService.createBadge(member.getId(), member.getTotalCertifyCount()));
		assertThat(badgeRepository.existsByMemberIdAndType(member.getId(), expectedType))
			.isTrue();
	}
}
