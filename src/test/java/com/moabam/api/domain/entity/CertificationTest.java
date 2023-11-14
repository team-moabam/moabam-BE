package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;

class CertificationTest {

	String content = "물 마시기";
	String image = "https://s3.testtest";

	@DisplayName("Certification 생성 성공")
	@Test
	void create_certification_success() {
		Room room = Room.builder()
			.title("앵윤이의 방")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(9)
			.build();

		Routine routine = Routine.builder()
			.room(room)
			.content(content)
			.build();

		assertThatNoException().isThrownBy(() -> {
			Certification certification = Certification.builder()
				.routine(routine)
				.memberId(1L)
				.image(image).build();

			assertThat(certification.getImage()).isEqualTo(image);
			assertThat(certification.getMemberId()).isEqualTo(1L);
			assertThat(certification.getRoutine()).isEqualTo(routine);
		});
	}
}
