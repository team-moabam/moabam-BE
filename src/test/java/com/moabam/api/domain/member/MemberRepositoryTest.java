package com.moabam.api.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.member.repository.BadgeRepository;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.BadgeFixture;
import com.moabam.support.fixture.MemberFixture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@QuerydslRepositoryTest
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MemberSearchRepository memberSearchRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	BadgeRepository badgeRepository;

	@Autowired
	ParticipantRepository participantRepository;

	@PersistenceContext
	EntityManager entityManager;

	@DisplayName("회원 생성 테스트")
	@Test
	void test() {
		// given
		Member member = MemberFixture.member("313");
		memberRepository.save(member);

		// when
		Member savedMember = memberRepository.findBySocialId(member.getSocialId()).orElse(null);

		// then
		assertThat(savedMember).isNotNull();
	}

	@DisplayName("회원 정보 찾는 Query")
	@Nested
	class FindMemberInfo {

		@DisplayName("회원 없어서 실패")
		@Test
		void member_not_found() {
			// Given
			List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(999L, false);

			// When + Then
			assertThat(memberInfos).isEmpty();
		}

		@DisplayName("성공")
		@Test
		void search_info_success() {
			// given
			Member member = MemberFixture.member("hhhh");
			member.enterRoom(RoomType.MORNING);
			memberRepository.save(member);

			Badge birth = BadgeFixture.badge(member.getId(), BadgeType.BIRTH);
			Badge level50 = BadgeFixture.badge(member.getId(), BadgeType.LEVEL50);
			Badge level10 = BadgeFixture.badge(member.getId(), BadgeType.LEVEL10);
			List<Badge> badges = List.of(birth, level10, level50);
			badgeRepository.saveAll(badges);

			// when
			List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(member.getId(), true);

			// then
			assertThat(memberInfos).isNotEmpty();

			MemberInfoSearchResponse memberInfoSearchResponse = MemberMapper.toMemberInfoSearchResponse(memberInfos);
			assertThat(memberInfoSearchResponse.badges()).hasSize(badges.size());
		}

		@DisplayName("성공")
		@Test
		void no_badges_search_success() {
			// given
			Member member = MemberFixture.member("ttttt");
			member.enterRoom(RoomType.MORNING);
			memberRepository.save(member);

			// when
			List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(member.getId(), true);

			// then
			assertThat(memberInfos).isNotEmpty();

			MemberInfoSearchResponse memberInfoSearchResponse = MemberMapper.toMemberInfoSearchResponse(memberInfos);
			assertThat(memberInfoSearchResponse.badges()).isEmpty();
		}
	}

	@DisplayName("삭제된 회원 찾기 테스트")
	@Transactional
	@Test
	void findMemberTest() {
		// Given
		Member member = MemberFixture.member();

		// When
		memberRepository.save(member);

		member.delete(LocalDateTime.now());
		memberRepository.flush();
		memberRepository.delete(member);

		memberRepository.flush();

		// then
		Optional<Member> deletedMember = memberSearchRepository.findMember(member.getId(), false);

		Assertions.assertAll(
			() -> assertThat(deletedMember).isPresent(),
			() -> {
				Member delete = deletedMember.get();
				assertThat(delete.getSocialId()).contains("delete");
				assertThat(delete.getDeletedAt()).isNotNull();
			}
		);
	}
}
