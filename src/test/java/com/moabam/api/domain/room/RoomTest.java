package com.moabam.api.domain.room;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

class RoomTest {

	@DisplayName("비밀번호 없이 방 생성 성공")
	@Test
	void create_room_without_password_success() {
		// given, when
		Room room = Room.builder()
			.title("앵윤이의 방")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(9)
			.build();

		// then
		assertThat(room.getPassword()).isNull();
		assertThat(room.getRoomImage()).isEqualTo("'https://image.moabam.com/moabam/default/room-level-00.png'");
		assertThat(room.getRoomType()).isEqualTo(RoomType.MORNING);
		assertThat(room.getCertifyTime()).isEqualTo(10);
		assertThat(room.getMaxUserCount()).isEqualTo(9);
		assertThat(room.getLevel()).isZero();
		assertThat(room.getCurrentUserCount()).isEqualTo(1);
		assertThat(room.getAnnouncement()).isNull();
	}

	@DisplayName("비밀번호 설정 후 방 생성 성공")
	@Test
	void create_room_with_password_success() {
		// given, when
		Room room = Room.builder()
			.title("앵윤이의 방")
			.password("12345")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(9)
			.build();

		// then
		assertThat(room.getPassword()).isEqualTo("12345");
	}

	@DisplayName("아침 방 설정 시, 저녁 시간이 들어오는 예외 발생")
	@ParameterizedTest
	@CsvSource({
		"13", "19", "3", "11", "0"
	})
	void morning_time_validate_exception(int certifyTime) {
		Room room = Room.builder()
			.title("모아밤 짱")
			.password("1234")
			.roomType(RoomType.MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		// given, when, then
		assertThatThrownBy(() -> room.changeCertifyTime(certifyTime))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_REQUEST_FIELD.getMessage());
	}

	@DisplayName("저녁 방 설정 시, 아침 시간이 들어오는 경우 예외 발생")
	@ParameterizedTest
	@CsvSource({
		"3", "5", "-1", "15", "8", "19"
	})
	void night_time_validate_exception(int certifyTime) {
		Room room = Room.builder()
			.title("모아밤 짱")
			.roomType(RoomType.NIGHT)
			.certifyTime(21)
			.maxUserCount(5)
			.build();

		// given, when, then
		assertThatThrownBy(() -> room.changeCertifyTime(certifyTime))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_REQUEST_FIELD.getMessage());
	}
}
