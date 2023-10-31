package com.moabam.api.presentation;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoutineRepository routineRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@DisplayName("비밀번호 없는 방 생성 성공")
	@Test
	void create_room_no_password_success() throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print());

		assertThat(roomRepository.findAll()).hasSize(1);
		assertThat(roomRepository.findAll().get(0).getTitle()).isEqualTo("재윤과 앵맹이의 방임");
		assertThat(roomRepository.findAll().get(0).getPassword()).isNull();
	}

	@DisplayName("비밀번호 있는 방 생성 성공")
	@ParameterizedTest
	@CsvSource({
		"1234", "12345678", "98765"
	})
	void create_room_with_password_success(String password) throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 있는 재윤과 앵맹이의 방임", password, routines, MORNING, 10, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print());

		assertThat(roomRepository.findAll()).hasSize(1);
		assertThat(roomRepository.findAll().get(0).getTitle()).isEqualTo("비번 있는 재윤과 앵맹이의 방임");
		assertThat(roomRepository.findAll().get(0).getPassword()).isEqualTo(password);
	}

	@DisplayName("올바르지 않은 비밀번호 방 생성시 예외 발생")
	@ParameterizedTest
	@CsvSource({
		"1", "12", "123", "123456789", "abc"
	})
	void create_room_with_wrong_password_fail(String password) throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 있는 재윤과 앵맹이의 방임", password, routines, MORNING, 10, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("Routine 갯수를 초과한 방 생성시 예외 발생")
	@Test
	void create_room_with_too_many_routine_fail() throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");
		routines.add("밥 먹기");
		routines.add("코드 리뷰 달기");
		routines.add("책 읽기");
		routines.add("산책 하기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 없는 재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("Routine 없는 방 생성시 예외 발생")
	@Test
	void create_room_with_no_routine_fail() throws Exception {
		// given
		List<String> routines = new ArrayList<>();

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 없는 재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("올바르지 못한 시간으로 아침 방 생성")
	@ParameterizedTest
	@CsvSource({
		"1", "3", "11", "12", "20"
	})
	void create_morning_room_wrong_certify_time_fail(int certifyTime) throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 없는 재윤과 앵맹이의 방임", null, routines, MORNING, certifyTime, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("올바르지 못한 시간으로 저녁 방 생성시 에외 발생")
	@ParameterizedTest
	@CsvSource({
		"19", "3", "6", "9"
	})
	void create_night_room_wrong_certify_time_fail(int certifyTime) throws Exception {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"비번 없는 재윤과 앵맹이의 방임", null, routines, NIGHT, certifyTime, 4);

		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("방 수정 성공 - 방장일 경우")
	@Test
	void modify_room_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		Participant participant = Participant.builder()
			.room(room)
			.memberId(1L)
			.build();
		participant.enableManager();

		Room savedRoom = roomRepository.save(room);
		participantRepository.save(participant);

		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "1234", 10, 7);

		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + savedRoom.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 수정 실패 - 방장 아닐 경우")
	@Test
	void unauthorized_modify_room_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		Participant participant = Participant.builder()
			.room(room)
			.memberId(1L)
			.build();

		Room savedRoom = roomRepository.save(room);
		participantRepository.save(participant);

		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "1234", 10, 7);

		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + savedRoom.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andDo(print());
	}
}
