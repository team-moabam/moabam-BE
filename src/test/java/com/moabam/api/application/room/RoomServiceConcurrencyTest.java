package com.moabam.api.application.room;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@SpringBootTest
class RoomServiceConcurrencyTest {

	@Autowired
	private RoomService roomService;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private ParticipantSearchRepository participantSearchRepository;

	@Autowired
	private MemberRepository memberRepository;

	@DisplayName("입장 가능이 1명이 남은 상태에서 3명 동시 입장 요청")
	@Test
	void enter_room_concurrency_test() throws InterruptedException {
		// given
		Room room = Room.builder()
			.title("테스트방")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(4)
			.build();

		for (int i = 0; i < 2; i++) {
			room.increaseCurrentUserCount();
		}

		Room savedRoom = roomRepository.save(room);

		Member member1 = MemberFixture.member("qwe");
		Member member2 = MemberFixture.member("qwfe");
		Member member3 = MemberFixture.member("qff");
		memberRepository.saveAll(List.of(member1, member2, member3));

		Participant participant1 = RoomFixture.participant(savedRoom, member1.getId());
		Participant participant2 = RoomFixture.participant(savedRoom, member2.getId());
		Participant participant3 = RoomFixture.participant(savedRoom, member3.getId());
		participantRepository.saveAll(List.of(participant1, participant2, participant3));

		int threadCount = 3;
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		EnterRoomRequest enterRoomRequest = new EnterRoomRequest(null);
		List<Member> newMembers = new ArrayList<>();

		// when
		for (int i = 0; i < threadCount; i++) {
			Member member = MemberFixture.member(String.valueOf(i + 100));
			newMembers.add(member);
			memberRepository.save(member);
			final Long memberId = member.getId();

			executorService.submit(() -> {
				try {
					roomService.enterRoom(memberId, room.getId(), enterRoomRequest);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		List<Participant> actual = participantSearchRepository.findAllByRoomId(room.getId());
		Member newMember1 = memberRepository.findById(newMembers.get(0).getId()).orElseThrow();
		Member newMember2 = memberRepository.findById(newMembers.get(1).getId()).orElseThrow();
		Member newMember3 = memberRepository.findById(newMembers.get(2).getId()).orElseThrow();

		// then
		assertThat(actual).hasSize(4);
		assertThat(newMember1.getCurrentMorningCount() + newMember2.getCurrentMorningCount()
			+ newMember3.getCurrentMorningCount()).isEqualTo(1);

		memberRepository.deleteAllById(List.of(member1.getId(), member2.getId(), member3.getId()));
		memberRepository.deleteAll(newMembers);
	}
}
