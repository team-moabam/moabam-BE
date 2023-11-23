package com.moabam.api.application.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.spy;
import static org.mockito.BDDMockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoomSearchRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.room.GetAllRoomsResponse;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.RoomsHistoryResponse;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

	@InjectMocks
	private SearchService searchService;

	@Mock
	private CertificationsSearchRepository certificationsSearchRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private RoomSearchRepository roomSearchRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private CertificationService certificationService;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private ClockHolder clockHolder;

	@DisplayName("유저가 참여중인 방 목록 조회 성공")
	@Test
	void get_my_rooms_success() {
		// given
		LocalDate today = LocalDate.now();
		Long memberId = 1L;
		Room room1 = spy(RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10));
		Room room2 = spy(RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9));
		Room room3 = spy(RoomFixture.room("밤 - 첫 번째 방", RoomType.NIGHT, 22));

		when(room1.getId()).thenReturn(1L);
		when(room2.getId()).thenReturn(2L);
		when(room3.getId()).thenReturn(3L);

		Participant participant1 = RoomFixture.participant(room1, memberId);
		Participant participant2 = RoomFixture.participant(room2, memberId);
		Participant participant3 = RoomFixture.participant(room3, memberId);
		List<Participant> participants = List.of(participant1, participant2, participant3);

		given(participantSearchRepository.findNotDeletedParticipantsByMemberId(memberId)).willReturn(participants);
		given(certificationService.existsMemberCertification(memberId, room1.getId(), today)).willReturn(true);
		given(certificationService.existsMemberCertification(memberId, room2.getId(), today)).willReturn(false);
		given(certificationService.existsMemberCertification(memberId, room3.getId(), today)).willReturn(true);

		given(certificationService.existsRoomCertification(room1.getId(), today)).willReturn(true);
		given(certificationService.existsRoomCertification(room2.getId(), today)).willReturn(false);
		given(certificationService.existsRoomCertification(room3.getId(), today)).willReturn(false);

		given(clockHolder.date()).willReturn(LocalDate.now());

		// when
		MyRoomsResponse myRooms = searchService.getMyRooms(memberId);

		// then
		assertThat(myRooms.participatingRooms()).hasSize(3);

		assertThat(myRooms.participatingRooms().get(0).isMemberCertifiedToday()).isTrue();
		assertThat(myRooms.participatingRooms().get(0).isRoomCertifiedToday()).isTrue();

		assertThat(myRooms.participatingRooms().get(1).isMemberCertifiedToday()).isFalse();
		assertThat(myRooms.participatingRooms().get(1).isRoomCertifiedToday()).isFalse();

		assertThat(myRooms.participatingRooms().get(2).isMemberCertifiedToday()).isTrue();
		assertThat(myRooms.participatingRooms().get(2).isRoomCertifiedToday()).isFalse();
	}

	@DisplayName("방 참여 기록 조회 성공")
	@Test
	void get_my_join_history_success() {
		// given
		LocalDateTime today = LocalDateTime.now();
		Long memberId = 1L;
		Room room1 = spy(RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10));
		Room room2 = spy(RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9));
		Room room3 = RoomFixture.room("밤 - 첫 번째 방", RoomType.NIGHT, 22);

		when(room1.getId()).thenReturn(1L);
		when(room2.getId()).thenReturn(2L);

		Participant participant1 = RoomFixture.participant(room1, memberId);
		Participant participant2 = RoomFixture.participant(room2, memberId);
		Participant participant3 = spy(RoomFixture.participant(room3, memberId));
		participant3.removeRoom();
		List<Participant> participants = List.of(participant1, participant2, participant3);

		when(participant3.getDeletedAt()).thenReturn(today);
		when(participant3.getDeletedRoomTitle()).thenReturn("밤 - 첫 번째 방");
		given(participantSearchRepository.findAllParticipantsByMemberId(memberId)).willReturn(participants);

		// when
		RoomsHistoryResponse response = searchService.getJoinHistory(memberId);

		// then
		assertThat(response.roomHistory()).hasSize(3);

		assertThat(response.roomHistory().get(0).deletedAt()).isNull();
		assertThat(response.roomHistory().get(0).title()).isEqualTo(room1.getTitle());

		assertThat(response.roomHistory().get(1).deletedAt()).isNull();
		assertThat(response.roomHistory().get(1).title()).isEqualTo(room2.getTitle());

		assertThat(response.roomHistory().get(2).deletedAt()).isNotNull();
		assertThat(response.roomHistory().get(2).title()).isEqualTo(participant3.getDeletedRoomTitle());
	}

	@DisplayName("아침, 저녁 전체 방 조회 성공, 첫 번째 조회, 다음 페이지 있음")
	@Test
	void search_all_morning_night_rooms_success() {
		// given
		Room room1 = spy(RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234"));
		Room room2 = spy(RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9));
		Room room3 = spy(RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22));
		Room room4 = spy(RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7));
		Room room5 = spy(RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869"));
		Room room6 = spy(RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8));
		Room room7 = spy(RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20));
		Room room8 = spy(RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236"));
		Room room9 = spy(RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4));
		Room room10 = spy(RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979"));
		Room room11 = spy(RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22));
		Room room12 = spy(RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10));
		Room room13 = spy(RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2));
		Room room14 = spy(RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21));

		given(room1.getId()).willReturn(1L);
		given(room2.getId()).willReturn(2L);
		given(room3.getId()).willReturn(3L);
		given(room4.getId()).willReturn(4L);
		given(room5.getId()).willReturn(5L);
		given(room6.getId()).willReturn(6L);
		given(room7.getId()).willReturn(7L);
		given(room8.getId()).willReturn(8L);
		given(room9.getId()).willReturn(9L);
		given(room10.getId()).willReturn(10L);
		given(room11.getId()).willReturn(11L);
		given(room12.getId()).willReturn(12L);
		given(room13.getId()).willReturn(13L);
		given(room14.getId()).willReturn(14L);

		List<Room> rooms = List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11);

		Routine routine1 = spy(RoomFixture.routine(room1, "방1의 루틴1"));
		Routine routine2 = spy(RoomFixture.routine(room1, "방1의 루틴2"));

		Routine routine3 = spy(RoomFixture.routine(room2, "방2의 루틴1"));
		Routine routine4 = spy(RoomFixture.routine(room2, "방2의 루틴2"));

		Routine routine5 = spy(RoomFixture.routine(room3, "방3의 루틴1"));
		Routine routine6 = spy(RoomFixture.routine(room3, "방3의 루틴2"));

		Routine routine7 = spy(RoomFixture.routine(room4, "방4의 루틴1"));
		Routine routine8 = spy(RoomFixture.routine(room4, "방4의 루틴2"));

		Routine routine9 = spy(RoomFixture.routine(room5, "방5의 루틴1"));
		Routine routine10 = spy(RoomFixture.routine(room5, "방5의 루틴2"));

		Routine routine11 = spy(RoomFixture.routine(room6, "방6의 루틴1"));
		Routine routine12 = spy(RoomFixture.routine(room6, "방6의 루틴2"));

		Routine routine13 = spy(RoomFixture.routine(room7, "방7의 루틴1"));
		Routine routine14 = spy(RoomFixture.routine(room7, "방7의 루틴2"));

		Routine routine15 = spy(RoomFixture.routine(room8, "방8의 루틴1"));
		Routine routine16 = spy(RoomFixture.routine(room8, "방8의 루틴2"));

		Routine routine17 = spy(RoomFixture.routine(room9, "방9의 루틴1"));
		Routine routine18 = spy(RoomFixture.routine(room9, "방9의 루틴2"));

		Routine routine19 = spy(RoomFixture.routine(room10, "방10의 루틴1"));
		Routine routine20 = spy(RoomFixture.routine(room10, "방10의 루틴2"));

		Routine routine21 = spy(RoomFixture.routine(room11, "방11의 루틴1"));
		Routine routine22 = spy(RoomFixture.routine(room11, "방11의 루틴2"));

		Routine routine23 = spy(RoomFixture.routine(room12, "방12의 루틴1"));
		Routine routine24 = spy(RoomFixture.routine(room12, "방12의 루틴2"));

		Routine routine25 = spy(RoomFixture.routine(room13, "방13의 루틴1"));
		Routine routine26 = spy(RoomFixture.routine(room13, "방13의 루틴2"));

		Routine routine27 = spy(RoomFixture.routine(room14, "방14의 루틴1"));
		Routine routine28 = spy(RoomFixture.routine(room14, "방14의 루틴2"));

		given(routine1.getId()).willReturn(1L);
		given(routine2.getId()).willReturn(2L);
		given(routine3.getId()).willReturn(3L);
		given(routine4.getId()).willReturn(4L);
		given(routine5.getId()).willReturn(5L);
		given(routine6.getId()).willReturn(6L);
		given(routine7.getId()).willReturn(7L);
		given(routine8.getId()).willReturn(8L);
		given(routine9.getId()).willReturn(9L);
		given(routine10.getId()).willReturn(10L);
		given(routine11.getId()).willReturn(11L);
		given(routine12.getId()).willReturn(12L);
		given(routine13.getId()).willReturn(13L);
		given(routine14.getId()).willReturn(14L);
		given(routine15.getId()).willReturn(15L);
		given(routine16.getId()).willReturn(16L);
		given(routine17.getId()).willReturn(17L);
		given(routine18.getId()).willReturn(18L);
		given(routine19.getId()).willReturn(19L);
		given(routine20.getId()).willReturn(20L);

		List<Routine> routines = List.of(routine1, routine2, routine3, routine4, routine5, routine6, routine7, routine8,
			routine9, routine10, routine11, routine12, routine13, routine14, routine15, routine16, routine17, routine18,
			routine19, routine20, routine21, routine22, routine23, routine24, routine25, routine26, routine27,
			routine28);

		given(roomSearchRepository.findAllWithNoOffset(null, null)).willReturn(rooms);
		given(routineRepository.findAllByRoomIdIn(anyList())).willReturn(routines);

		// when
		GetAllRoomsResponse getAllRoomsResponse = searchService.getAllRooms(null, null);

		// then
		assertThat(getAllRoomsResponse.hasNext()).isTrue();
		assertThat(getAllRoomsResponse.rooms()).hasSize(10);
		assertThat(getAllRoomsResponse.rooms().get(0).id()).isEqualTo(1L);
		assertThat(getAllRoomsResponse.rooms().get(9).id()).isEqualTo(10L);
	}

	@DisplayName("아침, 저녁 전체 방 조회 성공, 마지막 페이 조회, 다음 페이지 없음")
	@Test
	void search_last_page_all_morning_night_rooms_success() {
		// given
		Room room11 = spy(RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22));
		Room room12 = spy(RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10));
		Room room13 = spy(RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2));
		Room room14 = spy(RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21));

		given(room11.getId()).willReturn(11L);
		given(room12.getId()).willReturn(12L);
		given(room13.getId()).willReturn(13L);
		given(room14.getId()).willReturn(14L);

		List<Room> rooms = List.of(room11, room12, room13, room14);

		Routine routine21 = spy(RoomFixture.routine(room11, "방11의 루틴1"));
		Routine routine22 = spy(RoomFixture.routine(room11, "방11의 루틴2"));

		Routine routine23 = spy(RoomFixture.routine(room12, "방12의 루틴1"));
		Routine routine24 = spy(RoomFixture.routine(room12, "방12의 루틴2"));

		Routine routine25 = spy(RoomFixture.routine(room13, "방13의 루틴1"));
		Routine routine26 = spy(RoomFixture.routine(room13, "방13의 루틴2"));

		Routine routine27 = spy(RoomFixture.routine(room14, "방14의 루틴1"));
		Routine routine28 = spy(RoomFixture.routine(room14, "방14의 루틴2"));

		given(routine21.getId()).willReturn(21L);
		given(routine22.getId()).willReturn(22L);
		given(routine23.getId()).willReturn(23L);
		given(routine24.getId()).willReturn(24L);
		given(routine25.getId()).willReturn(25L);
		given(routine26.getId()).willReturn(26L);
		given(routine27.getId()).willReturn(27L);
		given(routine28.getId()).willReturn(28L);

		List<Routine> routines = List.of(routine21, routine22, routine23, routine24, routine25, routine26, routine27,
			routine28);

		given(roomSearchRepository.findAllWithNoOffset(null, 10L)).willReturn(rooms);
		given(routineRepository.findAllByRoomIdIn(anyList())).willReturn(routines);

		// when
		GetAllRoomsResponse getAllRoomsResponse = searchService.getAllRooms(null, 10L);

		// then
		assertThat(getAllRoomsResponse.hasNext()).isFalse();
		assertThat(getAllRoomsResponse.rooms()).hasSize(4);
		assertThat(getAllRoomsResponse.rooms().get(0).id()).isEqualTo(11L);
		assertThat(getAllRoomsResponse.rooms().get(3).id()).isEqualTo(14L);
	}

	@DisplayName("전체 방 제목, 방장 이름, 루틴 내용으로 검색 성공 - 최초 조회")
	@Test
	void search_room_by_title_manager_nickname_routine_success() {
		// given
		Room room1 = spy(RoomFixture.room("아침 - 첫 번째 방", RoomType.MORNING, 10, "1234"));
		Room room2 = spy(RoomFixture.room("아침 - 두 번째 방", RoomType.MORNING, 9));
		Room room3 = spy(RoomFixture.room("밤 - 세 번째 방", RoomType.NIGHT, 22));
		Room room4 = spy(RoomFixture.room("아침 - 네 번째 방", RoomType.MORNING, 7));
		Room room5 = spy(RoomFixture.room("밤 - 다섯 번째 방", RoomType.NIGHT, 23, "5869"));
		Room room6 = spy(RoomFixture.room("아침 - 여섯 번째 방", RoomType.MORNING, 8));
		Room room7 = spy(RoomFixture.room("밤 - 일곱 번째 방", RoomType.NIGHT, 20));
		Room room8 = spy(RoomFixture.room("밤 - 여덟 번째 방", RoomType.NIGHT, 1, "5236"));
		Room room9 = spy(RoomFixture.room("아침 - 아홉 번째 방", RoomType.MORNING, 4));
		Room room10 = spy(RoomFixture.room("밤 - 열 번째 방", RoomType.NIGHT, 1, "97979"));
		Room room11 = spy(RoomFixture.room("밤 - 열하나 번째 방", RoomType.NIGHT, 22));
		Room room12 = spy(RoomFixture.room("아침 - 열둘 번째 방", RoomType.MORNING, 10));
		Room room13 = spy(RoomFixture.room("밤 - 열셋 번째 방", RoomType.NIGHT, 2));
		Room room14 = spy(RoomFixture.room("밤 - 열넷 번째 방", RoomType.NIGHT, 21));

		given(room4.getId()).willReturn(4L);
		given(room5.getId()).willReturn(5L);
		given(room6.getId()).willReturn(6L);
		given(room7.getId()).willReturn(7L);
		given(room8.getId()).willReturn(8L);
		given(room9.getId()).willReturn(9L);
		given(room10.getId()).willReturn(10L);
		given(room11.getId()).willReturn(11L);
		given(room12.getId()).willReturn(12L);
		given(room13.getId()).willReturn(13L);
		given(room14.getId()).willReturn(14L);

		List<Room> rooms = List.of(room4, room5, room6, room7, room8, room9, room10, room11, room12, room13, room14);

		Routine routine9 = spy(RoomFixture.routine(room5, "방5의 루틴1"));
		Routine routine10 = spy(RoomFixture.routine(room5, "방5의 루틴2"));

		Routine routine11 = spy(RoomFixture.routine(room6, "방6의 루틴1"));
		Routine routine12 = spy(RoomFixture.routine(room6, "방6의 루틴2"));

		Routine routine13 = spy(RoomFixture.routine(room7, "방7의 루틴1"));
		Routine routine14 = spy(RoomFixture.routine(room7, "방7의 루틴2"));

		Routine routine15 = spy(RoomFixture.routine(room8, "방8의 루틴1"));
		Routine routine16 = spy(RoomFixture.routine(room8, "방8의 루틴2"));

		Routine routine17 = spy(RoomFixture.routine(room9, "방9의 루틴1"));
		Routine routine18 = spy(RoomFixture.routine(room9, "방9의 루틴2"));

		Routine routine19 = spy(RoomFixture.routine(room10, "방10의 루틴1"));
		Routine routine20 = spy(RoomFixture.routine(room10, "방10의 루틴2"));

		Routine routine21 = spy(RoomFixture.routine(room11, "방11의 루틴1"));
		Routine routine22 = spy(RoomFixture.routine(room11, "방11의 루틴2"));

		Routine routine23 = spy(RoomFixture.routine(room12, "방12의 루틴1"));
		Routine routine24 = spy(RoomFixture.routine(room12, "방12의 루틴2"));

		Routine routine25 = spy(RoomFixture.routine(room13, "방13의 루틴1"));
		Routine routine26 = spy(RoomFixture.routine(room13, "방13의 루틴2"));

		Routine routine27 = spy(RoomFixture.routine(room14, "방14의 루틴1"));
		Routine routine28 = spy(RoomFixture.routine(room14, "방14의 루틴2"));

		given(routine9.getId()).willReturn(9L);
		given(routine10.getId()).willReturn(10L);
		given(routine11.getId()).willReturn(11L);
		given(routine12.getId()).willReturn(12L);
		given(routine13.getId()).willReturn(13L);
		given(routine14.getId()).willReturn(14L);
		given(routine15.getId()).willReturn(15L);
		given(routine16.getId()).willReturn(16L);
		given(routine17.getId()).willReturn(17L);
		given(routine18.getId()).willReturn(18L);
		given(routine19.getId()).willReturn(19L);
		given(routine20.getId()).willReturn(20L);
		given(routine21.getId()).willReturn(21L);
		given(routine22.getId()).willReturn(22L);
		given(routine23.getId()).willReturn(23L);
		given(routine24.getId()).willReturn(24L);
		given(routine25.getId()).willReturn(25L);
		given(routine26.getId()).willReturn(26L);

		List<Routine> routines = List.of(routine9, routine10, routine11, routine12, routine13, routine14, routine15,
			routine16, routine17, routine18, routine19, routine20, routine21, routine22, routine23, routine24,
			routine25, routine26, routine27, routine28);

		given(roomRepository.searchByKeyword("번째")).willReturn(rooms);
		given(routineRepository.findAllByRoomIdIn(anyList())).willReturn(routines);

		// when
		GetAllRoomsResponse getAllRoomsResponse = searchService.searchRooms("번째", null, null);

		// then
		assertThat(getAllRoomsResponse.hasNext()).isTrue();
		assertThat(getAllRoomsResponse.rooms()).hasSize(10);
	}
}
