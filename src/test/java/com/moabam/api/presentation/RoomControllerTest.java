package com.moabam.api.presentation;

import static com.moabam.api.domain.room.RoomType.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationRepository;
import com.moabam.api.domain.room.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.room.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.domain.room.repository.RoutineSearchRepository;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.BugFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoomControllerTest extends WithoutFilterSupporter {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoutineRepository routineRepository;

	@Autowired
	private RoutineSearchRepository routineSearchRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CertificationRepository certificationRepository;

	@Autowired
	private DailyMemberCertificationRepository dailyMemberCertificationRepository;

	@Autowired
	private DailyRoomCertificationRepository dailyRoomCertificationRepository;

	@Autowired
	private ParticipantSearchRepository participantSearchRepository;

	Member member;

	@BeforeAll
	void setUp() {
		member = MemberFixture.member();
		memberRepository.save(member);
	}

	@AfterEach
	void cleanUp() {
		while (member.getCurrentMorningCount() > 0) {
			member.exitMorningRoom();
		}

		while (member.getCurrentNightCount() > 0) {
			member.exitNightRoom();
		}
	}

	@DisplayName("비밀번호 없는 방 생성 성공")
	@WithMember
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
	@WithMember
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
			"비번 있는 재맹의 방임", password, routines, MORNING, 10, 4);
		String json = objectMapper.writeValueAsString(createRoomRequest);

		// expected
		mockMvc.perform(post("/rooms")
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andDo(print());

		assertThat(roomRepository.findAll()).hasSize(1);
		assertThat(roomRepository.findAll().get(0).getTitle()).isEqualTo("비번 있는 재맹의 방임");
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

	@DisplayName("올바르지 못한 시간으로 아침 방 생성시 예외 발생")
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
	@WithMember(id = 1L)
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

		List<Routine> routines = RoomFixture.routines(room);

		Participant participant = RoomFixture.participant(room, 1L);
		participant.enableManager();

		List<String> newRoutines = new ArrayList<>();
		newRoutines.add("물 마시기");
		newRoutines.add("코테 풀기");

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);

		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "공지공지", newRoutines, "4567", 10, 7);
		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		Room modifiedRoom = roomRepository.findById(room.getId()).orElseThrow();
		List<Routine> modifiedRoutines = routineSearchRepository.findAllByRoomId(room.getId());

		assertThat(modifiedRoom.getTitle()).isEqualTo("수정할 방임!");
		assertThat(modifiedRoom.getCertifyTime()).isEqualTo(10);
		assertThat(modifiedRoom.getPassword()).isEqualTo("4567");
		assertThat(modifiedRoom.getAnnouncement()).isEqualTo("공지공지");
		assertThat(modifiedRoom.getMaxUserCount()).isEqualTo(7);
		assertThat(modifiedRoutines).hasSize(2);
	}

	@DisplayName("방 수정 실패 - 방장 아닐 경우")
	@WithMember(id = 1L)
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

		Participant participant = RoomFixture.participant(room, 1L);

		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		roomRepository.save(room);
		participantRepository.save(participant);
		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "방 공지", routines, "1234", 9, 7);
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
	@WithMember(id = 1L)
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
	@WithMember(id = 1L)
	@Test
	void enter_room_with_no_password_success() throws Exception {
		// given
		Room room = RoomFixture.room();

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
	@WithMember(id = 1L)
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
	@WithMember(id = 1L)
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
	@WithMember(id = 1L)
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
	@WithMember(id = 1L)
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
			.socialId(1L)
			.nickname("nick")
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
	@WithMember(id = 1L)
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
	@WithMember(id = 1L)
	@Test
	void no_manager_exit_room_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("5명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(8)
			.build();

		Participant participant = RoomFixture.participant(room, 1L);

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
		Room findRoom = roomRepository.findById(room.getId()).orElseThrow();
		Participant deletedParticipant = participantRepository.findById(participant.getId()).orElseThrow();

		assertThat(findRoom.getCurrentUserCount()).isEqualTo(4);
		assertThat(deletedParticipant.getDeletedAt()).isNotNull();
	}

	@DisplayName("방장의 방 나가기 - 방 삭제 성공")
	@WithMember(id = 1L)
	@Test
	void manager_delete_room_success() throws Exception {
		// given
		Room room = Room.builder()
			.title("1명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(8)
			.build();

		Participant participant = RoomFixture.participant(room, 1L);
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
	@WithMember(id = 1L)
	@Test
	void manager_exit_room_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("7명이 있는 방~")
			.roomType(NIGHT)
			.certifyTime(21)
			.maxUserCount(10)
			.build();

		Participant participant = RoomFixture.participant(room, 1L);
		participant.enableManager();

		for (int i = 0; i < 6; i++) {
			room.increaseCurrentUserCount();
		}

		roomRepository.save(room);
		participantRepository.save(participant);
		String message = "{\"message\":\"인원수가 2명 이상일 때는 방장을 위임해야 합니다.\"}";

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isBadRequest())
			.andExpect(content().json(message))
			.andDo(print());
	}

	@DisplayName("아침 방 나가기 이후 사용자의 방 입장 횟수 감소 테스트")
	@WithMember(id = 1L)
	@Test
	void exit_and_decrease_morning_room_count() throws Exception {
		// given
		Room room = RoomFixture.room();

		Participant participant = RoomFixture.participant(room, 1L);

		for (int i = 0; i < 3; i++) {
			member.enterMorningRoom();
		}

		memberRepository.save(member);
		roomRepository.save(room);
		participantRepository.save(participant);

		// when
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isOk());

		Member getMember = memberRepository.findById(1L).orElseThrow();

		// then
		assertThat(getMember.getCurrentMorningCount()).isEqualTo(2);
	}

	@DisplayName("저녁 방 나가기 이후 사용자의 방 입장 횟수 감소 테스트")
	@WithMember(id = 1L)
	@Test
	void exit_and_decrease_night_room_count() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(NIGHT)
			.certifyTime(23)
			.maxUserCount(5)
			.build();

		Participant participant = RoomFixture.participant(room, 1L);

		for (int i = 0; i < 3; i++) {
			member.enterNightRoom();
		}

		memberRepository.save(member);
		roomRepository.save(room);
		participantRepository.save(participant);

		// when
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isOk());

		Member getMember = memberRepository.findById(1L).orElseThrow();

		// then
		assertThat(getMember.getCurrentNightCount()).isEqualTo(2);
	}

	@DisplayName("방 상세 정보 조회 성공 테스트")
	@WithMember(id = 1L)
	@Test
	void get_room_details_test() throws Exception {
		// given
		Room room = Room.builder()
			.title("방 제목")
			.password("1234")
			.roomType(NIGHT)
			.certifyTime(23)
			.maxUserCount(5)
			.build();

		room.increaseCurrentUserCount();
		room.increaseCurrentUserCount();

		List<Routine> routines = RoomFixture.routines(room);

		Participant participant1 = RoomFixture.participant(room, 1L);
		participant1.enableManager();

		Member member2 = MemberFixture.member(2L, "NICK2");
		Member member3 = MemberFixture.member(3L, "NICK3");

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		memberRepository.save(member2);
		memberRepository.save(member3);

		Participant participant2 = RoomFixture.participant(room, member2.getId());
		Participant participant3 = RoomFixture.participant(room, member3.getId());

		participantRepository.save(participant1);
		participantRepository.save(participant2);
		participantRepository.save(participant3);

		Certification certification1 = Certification.builder()
			.routine(routines.get(0))
			.memberId(member.getId())
			.image("member1Image")
			.build();

		Certification certification2 = Certification.builder()
			.routine(routines.get(1))
			.memberId(member.getId())
			.image("member2Image")
			.build();

		certificationRepository.save(certification1);
		certificationRepository.save(certification2);

		DailyMemberCertification dailyMemberCertification = RoomFixture.dailyMemberCertification(member.getId(),
			room.getId(), participant1);
		dailyMemberCertificationRepository.save(dailyMemberCertification);

		DailyRoomCertification dailyRoomCertification = RoomFixture.dailyRoomCertification(room.getId(),
			LocalDate.now());
		dailyRoomCertificationRepository.save(dailyRoomCertification);

		// expected
		mockMvc.perform(get("/rooms/" + room.getId()))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 추방 성공")
	@WithMember(id = 1L)
	@Test
	void deport_member_success() throws Exception {
		// given
		Room room = RoomFixture.room();
		Member member = MemberFixture.member(1234L, "참여자");
		memberRepository.save(member);

		Participant memberParticipant = RoomFixture.participant(room, member.getId());
		Participant managerParticipant = RoomFixture.participant(room, 1L);
		managerParticipant.enableManager();

		room.increaseCurrentUserCount();

		roomRepository.save(room);
		participantRepository.save(memberParticipant);
		participantRepository.save(managerParticipant);

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId() + "/members/" + member.getId()))
			.andExpect(status().isOk())
			.andDo(print());
		roomRepository.flush();

		Room getRoom = roomRepository.findById(room.getId()).orElseThrow();
		Participant getMemberParticipant = participantRepository.findById(memberParticipant.getId()).orElseThrow();

		assertThat(getRoom.getCurrentUserCount()).isEqualTo(1);
		assertThat(getMemberParticipant.getDeletedAt()).isNotNull();
		assertThat(participantSearchRepository.findOne(member.getId(), room.getId())).isEmpty();
	}

	@DisplayName("현재 참여중인 모든 방 조회 성공 - 첫번째 방은 개인과 방 모두 인증 성공")
	@WithMember(id = 1L)
	@Test
	void get_all_my_rooms_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", MORNING, 10);
		Room room2 = RoomFixture.room("아침 - 두 번째 방", MORNING, 8);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", NIGHT, 22);

		Participant participant1 = RoomFixture.participant(room1, 1L);
		Participant participant2 = RoomFixture.participant(room2, 1L);
		Participant participant3 = RoomFixture.participant(room3, 1L);

		DailyMemberCertification dailyMemberCertification = RoomFixture.dailyMemberCertification(1L, 1L, participant1);
		DailyRoomCertification dailyRoomCertification = RoomFixture.dailyRoomCertification(1L, LocalDate.now());

		roomRepository.saveAll(List.of(room1, room2, room3));
		participantRepository.saveAll(List.of(participant1, participant2, participant3));
		dailyMemberCertificationRepository.save(dailyMemberCertification);
		dailyRoomCertificationRepository.save(dailyRoomCertification);

		// expected
		mockMvc.perform(get("/rooms/my-join"))
			.andExpect(status().isOk())
			.andDo(print());
	}
}
