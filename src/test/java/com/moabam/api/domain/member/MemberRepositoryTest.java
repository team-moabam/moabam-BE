package com.moabam.api.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.member.repository.BadgeRepository;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.BadgeFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.ParticipantFixture;
import com.moabam.support.fixture.RoomFixture;

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

	@DisplayName("회원 생성 테스트")
	@Test
	void test() {
		// given
		Member member = MemberFixture.member();
		memberRepository.save(member);

		// when
		Member savedMember = memberRepository.findBySocialId(member.getSocialId()).orElse(null);

		// then
		assertThat(savedMember).isNotNull();
	}

	@DisplayName("회원 찾는 query 조회")
	@Nested
	class FindMemberTest {

		@DisplayName("회원이 방 매니저이면 에러")
		@Test
		void room_exist_and_manager_error() {
			// given
			Member member = MemberFixture.member();
			memberRepository.save(member);

			Room room = RoomFixture.room();
			roomRepository.save(room);

			Participant participant = ParticipantFixture.participant(room, member.getId());
			participant.enableManager();
			participantRepository.save(participant);

			// when
			Optional<Member> memberOptional =
				memberSearchRepository.findMemberNotManager(member.getId());

			// then
			assertThat(memberOptional).isEmpty();
		}

		@DisplayName("매니저가 아니면 회원 조회 성공")
		@Test
		void room_exist_and_not_manager_success() {
			// given
			Room room = RoomFixture.room();
			room.changeManagerNickname("test");
			roomRepository.save(room);

			Member member = MemberFixture.member();
			member.changeNickName("not");
			memberRepository.save(member);

			// when
			Optional<Member> memberOptional =
				memberSearchRepository.findMemberNotManager(member.getId());

			// then
			assertThat(memberOptional).isNotEmpty();
		}
	}

	@DisplayName("회원 정보 찾는 Query")
	@Nested
	class FindMemberInfo {

		@DisplayName("회원 없어서 실패")
		@Test
		void member_not_found() {
			// Given
			List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(1L, false);

			// When + Then
			assertThat(memberInfos).isEmpty();
		}

		@DisplayName("성공")
		@Test
		void search_info_success() {
			// given
			Member member = MemberFixture.member();
			member.enterMorningRoom();
			memberRepository.save(member);

			Badge morningBirth = BadgeFixture.badge(member.getId(), BadgeType.MORNING_BIRTH);
			Badge morningAdult = BadgeFixture.badge(member.getId(), BadgeType.MORNING_ADULT);
			Badge nightBirth = BadgeFixture.badge(member.getId(), BadgeType.NIGHT_BIRTH);
			Badge nightAdult = BadgeFixture.badge(member.getId(), BadgeType.NIGHT_ADULT);
			List<Badge> badges = List.of(morningBirth, morningAdult, nightBirth, nightAdult);
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
			Member member = MemberFixture.member();
			member.enterMorningRoom();
			memberRepository.save(member);

			// when
			List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(member.getId(), true);

			// then
			assertThat(memberInfos).isNotEmpty();

			MemberInfoSearchResponse memberInfoSearchResponse = MemberMapper.toMemberInfoSearchResponse(memberInfos);
			assertThat(memberInfoSearchResponse.badges()).isEmpty();
		}
	}
}
