package com.moabam.api.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@QuerydslRepositoryTest
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MemberSearchRepository memberSearchRepository;

	@Autowired
	RoomRepository roomRepository;

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
			Room room = RoomFixture.room();
			room.changeManagerNickname("nickname");
			roomRepository.save(room);

			Member member = MemberFixture.member();
			member.changeNickName("nickname");
			memberRepository.save(member);

			// when
			Optional<Member> memberOptional =
				memberSearchRepository.findMemberWithNotManger(member.getId());

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
				memberSearchRepository.findMemberWithNotManger(member.getId());

			// then
			assertThat(memberOptional).isNotEmpty();
		}
	}
}
