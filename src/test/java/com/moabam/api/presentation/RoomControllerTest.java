package com.moabam.api.presentation;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.EnterRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.fixture.BugFixture;
import com.moabam.fixture.MemberFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

	@Autowired
	private MemberRepository memberRepository;

	Member member;

	@BeforeAll
	void setUp() {
		member = MemberFixture.member();
		memberRepository.save(member);
	}

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
		roomRepository.save(room);
		participantRepository.save(participant);
		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "1234", 10, 7);
		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + room.getId())
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
		roomRepository.save(room);
		participantRepository.save(participant);
		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "1234", 10, 7);
		String json = objectMapper.writeValueAsString(modifyRoomRequest);
		String message = "{\"message\":\"방장이 아닌 사용자는 방을 수정할 수 없습니다.\"}";

		// expected
		mockMvc.perform(put("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isNotFound())
			.andExpect(content().json(message))
			.andDo(print());
	}

	@DisplayName("비밀번호 있는 방 참여 성공")
	@Test
	void enter_room_with_password_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("7777")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("7777");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// expected
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("비밀번호 없는 방 참여 성공")
	@Test
	void enter_room_with_no_password_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest(null);
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// expected
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 참여 후 인원수 증가 테스트")
	@Test
	void enter_and_increase_room_user_count() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// when
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk());

		Room findRoom = roomRepository.findById(room.getId()).orElseThrow();

		// then
		assertThat(findRoom.getCurrentUserCount()).isEqualTo(2);
	}

	@DisplayName("아침 방 참여 후 사용자의 방 입장 횟수 증가 테스트")
	@Test
	void enter_and_increase_morning_room_count() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// when
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk());

		Member getMember = memberRepository.findById(1L).orElseThrow();

		// then
		assertThat(getMember.getCurrentMorningCount()).isEqualTo(1);
		assertThat(getMember.getCurrentNightCount()).isZero();
	}

	@DisplayName("저녁 방 참여 후 사용자의 방 입장 횟수 증가 테스트")
	@Test
	void enter_and_increase_night_room_count() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(5)
			.build();

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// when
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk());

		Member getMember = memberRepository.findById(1L).orElseThrow();

		// then
		assertThat(getMember.getCurrentNightCount()).isEqualTo(1);
		assertThat(getMember.getCurrentMorningCount()).isZero();
	}

	@DisplayName("사용자의 아침 방 입장 횟수 3일시 예외 처리")
	@Test
	void enter_and_morning_room_over_three_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		for (int i = 0; i < 3; i++) {
			member.enterMorningRoom();
		}

		memberRepository.save(member);
		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// when
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("사용자의 저녁 방 입장 횟수 3일시 예외 처리")
	@Test
	void enter_and_night_room_over_three_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(NIGHT)
			.certifyTime(22)
			.maxUserCount(5)
			.build();

		for (int i = 0; i < 3; i++) {
			member.enterNightRoom();
		}

		memberRepository.save(member);
		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);

		// when
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("비밀번호 불일치 방 참여시 예외 발생")
	@Test
	void enter_room_wrong_password_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("7777")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		Member member = Member.builder()
			.id(1L)
			.socialId("test123")
			.nickname("nick")
			.profileImage("testtests")
			.bug(BugFixture.bug())
			.build();

		memberRepository.save(member);
		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("1234");
		String json = objectMapper.writeValueAsString(enterRoomRequest);
		String message = "{\"message\":\"방의 비밀번호가 일치하지 않습니다.\"}";

		// expected
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(content().json(message))
			.andDo(print());
	}

	@DisplayName("인원수가 모두 찬 방 참여시 예외 발생")
	@Test
	void enter_max_user_room_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("7777")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();

		for (int i = 0; i < 4; i++) {
			room.increaseCurrentUserCount();
		}

		roomRepository.save(room);
		EnterRoomRequest enterRoomRequest = new EnterRoomRequest("7777");
		String json = objectMapper.writeValueAsString(enterRoomRequest);
		String message = "{\"message\":\"방의 인원수가 찼습니다.\"}";

		// expected
		mockMvc.perform(post("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(content().json(message))
			.andDo(print());
	}

	@DisplayName("일반 사용자의 방 나가기 성공")
	@Test
	void no_manager_exit_room_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("5명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(8)
			.build();
		Participant participant = Participant.builder()
			.room(room)
			.memberId(1L)
			.build();

		for (int i = 0; i < 4; i++) {
			room.increaseCurrentUserCount();
		}

		roomRepository.save(room);
		participantRepository.save(participant);

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isOk())
			.andDo(print());
		participantRepository.flush();
		Participant deletedParticipant = participantRepository.findById(participant.getId()).orElseThrow();
		assertThat(room.getCurrentUserCount()).isEqualTo(4);
		assertThat(deletedParticipant.getDeletedAt()).isNotNull();
		assertThat(deletedParticipant.getDeletedRoomTitle()).isEqualTo("5명이 있는 방~");
	}

	@DisplayName("방장의 방 나가기 - 방 삭제 성공")
	@Test
	void manager_delete_room_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("1명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(8)
			.build();
		Participant participant = Participant.builder()
			.room(room)
			.memberId(1L)
			.build();
		participant.enableManager();
		roomRepository.save(room);
		participantRepository.save(participant);

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isOk())
			.andDo(print());
		Participant deletedParticipant = participantRepository.findById(participant.getId()).orElseThrow();
		assertThat(roomRepository.findById(room.getId())).isEmpty();
		assertThat(deletedParticipant.getDeletedAt()).isNotNull();
	}

	@DisplayName("방장이 위임하지 않고 방 나가기 실패")
	@Test
	void manager_exit_room_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("7명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(10)
			.build();
		Participant participant = Participant.builder()
			.room(room)
			.memberId(1L)
			.build();
		participant.enableManager();

		for (int i = 0; i < 6; i++) {
			room.increaseCurrentUserCount();
		}

		roomRepository.save(room);
		participantRepository.save(participant);
		String message = "{\"message\":\"인원수가 2명 이상일때는 방장을 위임해야합니다.\"}";

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isBadRequest())
			.andExpect(content().json(message))
			.andDo(print());
	}
}
