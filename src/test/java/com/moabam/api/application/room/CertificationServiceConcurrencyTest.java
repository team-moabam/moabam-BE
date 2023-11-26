package com.moabam.api.application.room;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.moabam.api.application.room.mapper.CertificationsMapper;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.room.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@SpringBootTest
class CertificationServiceConcurrencyTest {

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CertificationService certificationService;

	@Autowired
	private DailyMemberCertificationRepository dailyMemberCertificationRepository;

	@Autowired
	private DailyRoomCertificationRepository dailyRoomCertificationRepository;

	@DisplayName("방의 모든 참여자의 요청으로 방에 대한 인증")
	@Test
	void certify_room_success() throws InterruptedException {
		// given
		Room room = RoomFixture.room("테스트 하는 방이요", RoomType.MORNING, 9);
		for (int i = 0; i < 4; i++) {
			room.increaseCurrentUserCount();
		}
		Room savedRoom = roomRepository.save(room);

		Member member1 = MemberFixture.member("0000", "닉네임1");
		Member member2 = MemberFixture.member("1234", "닉네임2");
		Member member3 = MemberFixture.member("5678", "닉네임3");
		Member member4 = MemberFixture.member("3333", "닉네임4");
		Member member5 = MemberFixture.member("5555", "닉네임5");

		List<Member> members = memberRepository.saveAll(List.of(member1, member2, member3, member4, member5));

		Participant participant1 = RoomFixture.participant(savedRoom, member1.getId());
		Participant participant2 = RoomFixture.participant(savedRoom, member2.getId());
		Participant participant3 = RoomFixture.participant(savedRoom, member3.getId());
		Participant participant4 = RoomFixture.participant(savedRoom, member4.getId());
		Participant participant5 = RoomFixture.participant(savedRoom, member5.getId());

		participantRepository.saveAll(List.of(participant1, participant2, participant3, participant4, participant5));

		DailyMemberCertification dailyMemberCertification1 = RoomFixture.dailyMemberCertification(member1.getId(),
			savedRoom.getId(), participant1);
		DailyMemberCertification dailyMemberCertification2 = RoomFixture.dailyMemberCertification(member2.getId(),
			savedRoom.getId(), participant2);
		DailyMemberCertification dailyMemberCertification3 = RoomFixture.dailyMemberCertification(member3.getId(),
			savedRoom.getId(), participant3);
		DailyMemberCertification dailyMemberCertification4 = RoomFixture.dailyMemberCertification(member4.getId(),
			savedRoom.getId(), participant4);
		DailyMemberCertification dailyMemberCertification5 = RoomFixture.dailyMemberCertification(member5.getId(),
			savedRoom.getId(), participant5);

		dailyMemberCertificationRepository.saveAll(
			List.of(dailyMemberCertification1, dailyMemberCertification2, dailyMemberCertification3,
				dailyMemberCertification4, dailyMemberCertification5));

		int threadCount = 5;
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		// when
		for (int i = 0; i < threadCount; i++) {
			final int currentIndex = i;

			executorService.submit(() -> {
				try {
					CertifiedMemberInfo certifiedMemberInfo = CertificationsMapper.toCertifiedMemberInfo(
						LocalDate.now(), BugType.MORNING, savedRoom, members.get(currentIndex));

					certificationService.certifyRoom(certifiedMemberInfo);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		Member savedMember1 = memberRepository.findById(member1.getId()).orElseThrow();
		List<DailyRoomCertification> dailyRoomCertification = dailyRoomCertificationRepository.findAll();
		assertThat(savedMember1.getBug().getMorningBug()).isEqualTo(11);
		assertThat(dailyRoomCertification).hasSize(1);

		participantRepository.deleteAll();
		memberRepository.deleteAllById(
			List.of(member1.getId(), member2.getId(), member3.getId(), member4.getId(), member5.getId()));
		dailyRoomCertificationRepository.deleteAll();
		dailyMemberCertificationRepository.deleteAll();
	}
}
