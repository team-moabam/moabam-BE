package com.moabam.api.domain.room.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.RoomFixture;

@QuerydslRepositoryTest
class CertificationsSearchRepositoryTest {

	@Autowired
	private CertificationsSearchRepository certificationsSearchRepository;

	@Autowired
	private CertificationRepository certificationRepository;

	@Autowired
	private DailyMemberCertificationRepository dailyMemberCertificationRepository;

	@Autowired
	private DailyRoomCertificationRepository dailyRoomCertificationRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoutineRepository routineRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@DisplayName("방에서 당일 유저들의 인증 조회")
	@Test
	void find_certifications_test() {
		// given
		Room room = RoomFixture.room();
		List<Routine> routines = RoomFixture.routines(room);
		Certification certification1 = RoomFixture.certification(routines.get(0));
		Certification certification2 = RoomFixture.certification(routines.get(1));

		Room savedRoom = roomRepository.save(room);
		routineRepository.save(routines.get(0));
		routineRepository.save(routines.get(1));
		certificationRepository.save(certification1);
		certificationRepository.save(certification2);

		// when
		List<Certification> actual = certificationsSearchRepository.findCertifications(savedRoom.getId(),
			LocalDate.now());

		//then
		assertThat(actual).hasSize(2)
			.containsExactly(certification1, certification2);
	}

	@DisplayName("당일 유저가 특정 방에서 인증 여부 조회")
	@Test
	void find_daily_member_certification() {
		// given
		Room room = roomRepository.save(RoomFixture.room());
		Participant participant = participantRepository.save(RoomFixture.participant(room, 1L));

		DailyMemberCertification dailyMemberCertification = RoomFixture.dailyMemberCertification(1L,
			room.getId(), participant);
		dailyMemberCertificationRepository.save(dailyMemberCertification);

		// when
		Optional<DailyMemberCertification> actual = certificationsSearchRepository.findDailyMemberCertification(1L,
			room.getId(), LocalDate.now());

		// then
		assertThat(actual)
			.isPresent()
			.contains(dailyMemberCertification);
	}

	@DisplayName("당일 방의 인증 여부 조회")
	@Test
	void find_daily_room_certification() {
		// given
		Room room = roomRepository.save(RoomFixture.room());
		DailyRoomCertification dailyRoomCertification = RoomFixture.dailyRoomCertification(room.getId(),
			LocalDate.now());
		dailyRoomCertificationRepository.save(dailyRoomCertification);

		// when
		Optional<DailyRoomCertification> actual = certificationsSearchRepository.findDailyRoomCertification(
			room.getId(), LocalDate.now());

		// then
		assertThat(actual)
			.isPresent()
			.contains(dailyRoomCertification);
	}
}
