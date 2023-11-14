package com.moabam.support.fixture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.mock.web.MockMultipartFile;

import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;

public class RoomFixture {

	public static Room room() {
		return Room.builder()
			.title("testTitle")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(8)
			.build();
	}

	public static Room room(int certifyTime) {
		return Room.builder()
			.title("testTitle")
			.roomType(RoomType.MORNING)
			.certifyTime(certifyTime)
			.maxUserCount(8)
			.build();
	}

	public static Participant participant(Room room, Long memberId) {
		return Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
	}

	public static List<Routine> routines(Room room) {
		List<Routine> routines = new ArrayList<>();

		Routine routine1 = Routine.builder()
			.room(room)
			.content("첫 루틴")
			.build();
		Routine routine2 = Routine.builder()
			.room(room)
			.content("두번째 루틴")
			.build();

		routines.add(routine1);
		routines.add(routine2);

		return routines;
	}

	public static Certification certification(Routine routine) {
		return Certification.builder()
			.routine(routine)
			.memberId(1L)
			.image("test1")
			.build();
	}

	public static DailyMemberCertification dailyMemberCertification(Long memberId, Long roomId,
		Participant participant) {
		return DailyMemberCertification.builder()
			.memberId(memberId)
			.roomId(roomId)
			.participant(participant)
			.build();
	}

	public static List<DailyMemberCertification> dailyMemberCertifications(Long roomId, Participant participant) {

		List<DailyMemberCertification> dailyMemberCertifications = new ArrayList<>();
		dailyMemberCertifications.add(DailyMemberCertification.builder()
			.roomId(roomId)
			.memberId(1L)
			.participant(participant)
			.build());
		dailyMemberCertifications.add(DailyMemberCertification.builder()
			.roomId(roomId)
			.memberId(2L)
			.participant(participant)
			.build());
		dailyMemberCertifications.add(DailyMemberCertification.builder()
			.roomId(roomId)
			.memberId(3L)
			.participant(participant)
			.build());

		return dailyMemberCertifications;
	}

	public static DailyRoomCertification dailyRoomCertification(Long roomId, LocalDate today) {
		return DailyRoomCertification.builder()
			.roomId(roomId)
			.certifiedAt(today)
			.build();
	}

	public static MockMultipartFile makeMultipartFile1() {
		try {
			File file = new File("src/test/resources/image.png");
			FileInputStream fileInputStream = new FileInputStream(file);

			return new MockMultipartFile("1", "image.png", "image/png", fileInputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static MockMultipartFile makeMultipartFile2() {
		try {
			File file = new File("src/test/resources/image.png");
			FileInputStream fileInputStream = new FileInputStream(file);

			return new MockMultipartFile("2", "image.png", "image/png", fileInputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static MockMultipartFile makeMultipartFile3() {
		try {
			File file = new File("src/test/resources/image.png");
			FileInputStream fileInputStream = new FileInputStream(file);

			return new MockMultipartFile("3", "image.png", "image/png", fileInputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
