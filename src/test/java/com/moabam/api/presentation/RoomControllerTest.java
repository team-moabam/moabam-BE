package com.moabam.api.presentation;

import static com.moabam.api.domain.room.RoomType.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationRepository;
import com.moabam.api.domain.room.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.room.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
import com.moabam.global.common.util.SystemClockHolder;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.BugFixture;
import com.moabam.support.fixture.InventoryFixture;
import com.moabam.support.fixture.ItemFixture;
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

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private InventoryRepository inventoryRepository;

	@SpyBean
	private SystemClockHolder clockHolder;

	Member member;

	@BeforeAll
	void setUp() {
		member = MemberFixture.member();
		memberRepository.save(member);
	}

	@AfterEach
	void cleanUp() {
		while (member.getCurrentMorningCount() > 0) {
			member.exitRoom(MORNING);
		}

		while (member.getCurrentNightCount() > 0) {
			member.exitRoom(NIGHT);
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

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);

		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "공지공지", "4567", 10, 7);
		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andDo(print());

		Room modifiedRoom = roomRepository.findById(room.getId()).orElseThrow();
		List<Routine> modifiedRoutines = routineRepository.findAllByRoomId(room.getId());

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

		roomRepository.save(room);
		participantRepository.save(participant);
		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "방 공지", "1234", 9, 7);
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

	@DisplayName("방 수정 실패 - 이미 한 참여자가 인증하고 방의 인증 시간을 바꾸려고 할때 예외 처리")
	@WithMember(id = 1L)
	@Test
	void room_certify_time_modification_fail() throws Exception {
		// given
		Room room = Room.builder()
			.title("처음 제목")
			.password("1234")
			.roomType(MORNING)
			.certifyTime(9)
			.maxUserCount(5)
			.build();
		room = roomRepository.save(room);

		Member member2 = MemberFixture.member("12313123");
		member2 = memberRepository.save(member2);

		Participant participant1 = RoomFixture.participant(room, 1L);
		participant1.enableManager();

		Participant participant2 = RoomFixture.participant(room, member2.getId());

		participantRepository.saveAll(List.of(participant1, participant2));

		DailyMemberCertification dailyMemberCertification = RoomFixture.dailyMemberCertification(member2.getId(),
			room.getId(), participant2);

		dailyMemberCertificationRepository.save(dailyMemberCertification);

		ModifyRoomRequest modifyRoomRequest = new ModifyRoomRequest("수정할 방임!", "방 공지", "1234", 10, 7);
		String json = objectMapper.writeValueAsString(modifyRoomRequest);

		// expected
		mockMvc.perform(put("/rooms/" + room.getId())
				.contentType(APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
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
		BDDMockito.given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 12, 3, 14, 30, 0));
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
			member.enterRoom(MORNING);
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
			member.enterRoom(NIGHT);
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
			.socialId("1")
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

		Room findRoom = roomRepository.findById(room.getId()).orElseThrow();
		List<Participant> deletedParticipant = participantRepository.findAll();

		assertThat(findRoom.getCurrentUserCount()).isEqualTo(4);
		assertThat(deletedParticipant).hasSize(1);
		assertThat(deletedParticipant.get(0).getDeletedAt()).isNotNull();
		assertThat(deletedParticipant.get(0).getDeletedRoomTitle()).isNotNull();
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

		List<Routine> routines = RoomFixture.routines(room);

		Participant participant = RoomFixture.participant(room, 1L);
		participant.enableManager();

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId()))
			.andExpect(status().isOk())
			.andDo(print());

		List<Room> deletedRoom = roomRepository.findAll();
		List<Routine> deletedRoutine = routineRepository.findAll();
		List<Participant> deletedParticipant = participantRepository.findAll();

		assertThat(deletedRoom).isEmpty();
		assertThat(deletedRoutine).hasSize(0);
		assertThat(deletedParticipant).hasSize(1);
		assertThat(deletedParticipant.get(0).getDeletedAt()).isNotNull();
		assertThat(deletedParticipant.get(0).getDeletedRoomTitle()).isNotNull();
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
			member.enterRoom(RoomType.MORNING);
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
			member.enterRoom(NIGHT);
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

		Member member2 = MemberFixture.member("2");
		Member member3 = MemberFixture.member("3");

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		member2 = memberRepository.save(member2);
		member3 = memberRepository.save(member3);

		Item item = ItemFixture.nightMageSkin();

		Inventory inventory1 = InventoryFixture.inventory(1L, item);
		Inventory inventory2 = InventoryFixture.inventory(member2.getId(), item);
		Inventory inventory3 = InventoryFixture.inventory(member3.getId(), item);
		inventory1.select(member);
		inventory2.select(member2);
		inventory3.select(member3);

		itemRepository.save(item);
		inventoryRepository.saveAll(List.of(inventory1, inventory2, inventory3));

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

		DailyRoomCertification dailyRoomCertification1 = RoomFixture.dailyRoomCertification(room.getId(),
			LocalDate.now().minusDays(3));
		dailyRoomCertificationRepository.save(dailyRoomCertification1);

		// expected
		mockMvc.perform(get("/rooms/" + room.getId() + "/" + LocalDate.now()))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 추방 성공")
	@WithMember(id = 1L)
	@Test
	void deport_member_success() throws Exception {
		// given
		Room room = RoomFixture.room();
		Member member = MemberFixture.member("1234");
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

	@DisplayName("방장 본인 추방 시도 - 예외 처리")
	@WithMember(id = 1L)
	@Test
	void deport_self_fail() throws Exception {
		// given
		Room room = RoomFixture.room();

		Participant managerParticipant = RoomFixture.participant(room, member.getId());
		managerParticipant.enableManager();

		roomRepository.save(room);
		participantRepository.save(managerParticipant);

		// expected
		mockMvc.perform(delete("/rooms/" + room.getId() + "/members/" + member.getId()))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@DisplayName("방장 위임 성공")
	@WithMember(id = 1L)
	@Test
	void mandate_manager_success() throws Exception {
		// given
		Member member2 = MemberFixture.member("1234");
		memberRepository.save(member2);

		Room room = RoomFixture.room();
		Participant participant1 = RoomFixture.participant(room, member.getId());
		participant1.enableManager();
		Participant participant2 = RoomFixture.participant(room, member2.getId());

		roomRepository.save(room);
		participantRepository.save(participant1);
		participantRepository.save(participant2);

		// expected
		mockMvc.perform(put("/rooms/" + room.getId() + "/members/" + member2.getId() + "/mandate"))
			.andExpect(status().isOk())
			.andDo(print());

		Room savedRoom = roomRepository.findById(room.getId()).orElseThrow();
		Participant savedParticipant1 = participantRepository.findById(participant1.getId()).orElseThrow();
		Participant savedParticipant2 = participantRepository.findById(participant2.getId()).orElseThrow();

		assertThat(savedRoom.getManagerNickname()).isEqualTo(member2.getNickname());
		assertThat(savedParticipant1.isManager()).isFalse();
		assertThat(savedParticipant2.isManager()).isTrue();
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

	@DisplayName("방 참여 기록 조회 성공")
	@WithMember(id = 1L)
	@Test
	void get_join_history_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", MORNING, 10);
		Room room2 = RoomFixture.room("아침 - 두 번째 방", MORNING, 8);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", NIGHT, 22);

		Participant participant1 = RoomFixture.participant(room1, 1L);
		Participant participant2 = RoomFixture.participant(room2, 1L);
		Participant participant3 = RoomFixture.participant(room3, 1L);

		roomRepository.saveAll(List.of(room1, room2, room3));
		participantRepository.saveAll(List.of(participant1, participant2, participant3));

		participant3.removeRoom();
		participantRepository.flush();
		participantRepository.delete(participant3);

		// expected
		mockMvc.perform(get("/rooms/join-history"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("참여중이지 않은 방에 대한 확인 성공")
	@WithMember
	@Test
	void check_if_participant_false_success() throws Exception {
		// given
		Room room = RoomFixture.room();
		Room savedRoom = roomRepository.save(room);

		// expected
		mockMvc.perform(get("/rooms/" + savedRoom.getId() + "/check"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("참여중이지 않은 방의 정보 불러오기 성공")
	@Test
	void get_un_joined_room_details() throws Exception {
		// given
		Room room = RoomFixture.room("테스트 방", NIGHT, 21);
		Room savedRoom = roomRepository.save(room);

		Member member1 = MemberFixture.member("901010");
		member1 = memberRepository.save(member1);

		Item item = ItemFixture.nightMageSkin();

		Inventory inventory = InventoryFixture.inventory(member1.getId(), item);
		inventory.select(member1);

		itemRepository.save(item);
		inventoryRepository.save(inventory);

		Participant participant = RoomFixture.participant(savedRoom, member1.getId());
		participantRepository.save(participant);

		Routine routine1 = RoomFixture.routine(savedRoom, "물 마시기");
		Routine routine2 = RoomFixture.routine(savedRoom, "커피 마시기");
		routineRepository.saveAll(List.of(routine1, routine2));

		DailyMemberCertification dailyMemberCertification = RoomFixture.dailyMemberCertification(member1.getId(),
			savedRoom.getId(), participant);
		dailyMemberCertificationRepository.save(dailyMemberCertification);

		// expected
		mockMvc.perform(get("/rooms/" + savedRoom.getId() + "/un-joined"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("참여중인 방에 대한 확인 성공")
	@WithMember
	@Test
	void check_if_participant_true_success() throws Exception {
		// given
		Room room = RoomFixture.room();
		Room savedRoom = roomRepository.save(room);

		Participant participant = RoomFixture.participant(room, 1L);
		participantRepository.save(participant);

		// expected
		mockMvc.perform(get("/rooms/" + savedRoom.getId() + "/check"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("아침, 저녁 방 전체 조회 성공 - 첫 번째 조회, 다음 페이지 있음")
	@WithMember(id = 1L)
	@Test
	void search_all_morning_night_rooms_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234");
		Room room2 = RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4);
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("아침, 저녁 방 전체 조회 성공 - 마지막 조회, 다음 페이지 없음")
	@WithMember(id = 1L)
	@Test
	void search_last_page_all_morning_night_rooms_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234");
		Room room2 = RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4);
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms?roomId=5"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("아침 방 전체 조회 성공 - 첫 번째 조회, 다음 페이지 없음")
	@WithMember(id = 1L)
	@Test
	void search_last_page_all_morning_rooms_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234");
		Room room2 = RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4);
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms?roomType=MORNING"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 검색 조회 성공 - 키워드만 존재")
	@WithMember(id = 1L)
	@Test
	void search_first_page_all_rooms_by_keyword_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234");
		Room room2 = RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4);
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms/search?keyword=아침"))
			.andExpect(status().isOk())
			.andDo(print());

		mockMvc.perform(get("/rooms/search?keyword=방12"))
			.andExpect(status().isOk())
			.andDo(print());

		mockMvc.perform(get("/rooms/search?keyword=방"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 검색 조회 성공 - 키워드 + 방 타입 존재")
	@WithMember(id = 1L)
	@Test
	void search_first_page_all_rooms_by_keyword_roomType_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234");
		Room room2 = RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4);
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms/search?keyword=번째&roomType=MORNING"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 검색 조회 성공 - 키워드 + 방 타입 + 추가 페이지 존재X")
	@WithMember(id = 1L)
	@Test
	void search_first_page_all_rooms_by_keyword_roomType_roomId_success() throws Exception {
		// given
		Room room1 = RoomFixture.room("밤 - 첫 번째 방", RoomType.NIGHT, 1, "1234");
		Room room2 = RoomFixture.room("밤 - 두 번째 방", RoomType.NIGHT, 1);
		Room room3 = RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22);
		Room room4 = RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7);
		Room room5 = RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869");
		Room room6 = RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8);
		Room room7 = RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20);
		Room room8 = RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236");
		Room room9 = RoomFixture.room("밤 - 아홉 번째 방", RoomType.NIGHT, 1, "5236");
		Room room10 = RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979");
		Room room11 = RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22);
		Room room12 = RoomFixture.room("밤 - 열둘 번째 방", RoomType.NIGHT, 1);
		Room room13 = RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2);
		Room room14 = RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21);

		Routine routine1 = RoomFixture.routine(room1, "방1의 루틴1");
		Routine routine2 = RoomFixture.routine(room1, "방1의 루틴2");

		Routine routine3 = RoomFixture.routine(room2, "방2의 루틴1");
		Routine routine4 = RoomFixture.routine(room2, "방2의 루틴2");

		Routine routine5 = RoomFixture.routine(room3, "방3의 루틴1");
		Routine routine6 = RoomFixture.routine(room3, "방3의 루틴2");

		Routine routine7 = RoomFixture.routine(room4, "방4의 루틴1");
		Routine routine8 = RoomFixture.routine(room4, "방4의 루틴2");

		Routine routine9 = RoomFixture.routine(room5, "방5의 루틴1");
		Routine routine10 = RoomFixture.routine(room5, "방5의 루틴2");

		Routine routine11 = RoomFixture.routine(room6, "방6의 루틴1");
		Routine routine12 = RoomFixture.routine(room6, "방6의 루틴2");

		Routine routine13 = RoomFixture.routine(room7, "방7의 루틴1");
		Routine routine14 = RoomFixture.routine(room7, "방7의 루틴2");

		Routine routine15 = RoomFixture.routine(room8, "방8의 루틴1");
		Routine routine16 = RoomFixture.routine(room8, "방8의 루틴2");

		Routine routine17 = RoomFixture.routine(room9, "방9의 루틴1");
		Routine routine18 = RoomFixture.routine(room9, "방9의 루틴2");

		Routine routine19 = RoomFixture.routine(room10, "방10의 루틴1");
		Routine routine20 = RoomFixture.routine(room10, "방10의 루틴2");

		Routine routine21 = RoomFixture.routine(room11, "방11의 루틴1");
		Routine routine22 = RoomFixture.routine(room11, "방11의 루틴2");

		Routine routine23 = RoomFixture.routine(room12, "방12의 루틴1");
		Routine routine24 = RoomFixture.routine(room12, "방12의 루틴2");

		Routine routine25 = RoomFixture.routine(room13, "방13의 루틴1");
		Routine routine26 = RoomFixture.routine(room13, "방13의 루틴2");

		Routine routine27 = RoomFixture.routine(room14, "방14의 루틴1");
		Routine routine28 = RoomFixture.routine(room14, "방14의 루틴2");

		roomRepository.saveAll(
			List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13,
				room14));

		routineRepository.saveAll(
			List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8, routine9, routine10,
				routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18, routine19,
				routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27, routine28));

		// expected
		mockMvc.perform(get("/rooms/search?keyword=루틴&roomType=NIGHT&roomId=3"))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@DisplayName("방 수정전 정보 불러오기 성공")
	@WithMember(id = 1L)
	@Test
	void get_room_details_before_modification_success() throws Exception {
		// given
		Member member2 = MemberFixture.member("123");
		Member member3 = MemberFixture.member("456");
		member2 = memberRepository.save(member2);
		member3 = memberRepository.save(member3);

		Room room = RoomFixture.room("수정 전 방 제목", MORNING, 10, "1234");
		Participant participant1 = RoomFixture.participant(room, 1L);
		participant1.enableManager();
		Participant participant2 = RoomFixture.participant(room, member2.getId());
		Participant participant3 = RoomFixture.participant(room, member3.getId());
		List<Routine> routines = RoomFixture.routines(room);

		roomRepository.save(room);
		participantRepository.saveAll(List.of(participant1, participant2, participant3));
		routineRepository.saveAll(routines);

		// expected
		mockMvc.perform(get("/rooms/" + room.getId()))
			.andExpect(status().isOk())
			.andDo(print());
	}
}
