package com.moabam.api.application.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.spy;
import static org.mockito.Mockito.when;

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
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoutineSearchRepository;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.RoomsHistoryResponse;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class RoomSearchServiceTest {

	@InjectMocks
	private RoomSearchService roomSearchService;

	@Mock
	private CertificationsSearchRepository certificationsSearchRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	@Mock
	private RoutineSearchRepository routineSearchRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private RoomCertificationService certificationService;

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

		// when
		MyRoomsResponse myRooms = roomSearchService.getMyRooms(memberId);

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
		RoomsHistoryResponse response = roomSearchService.getJoinHistory(memberId);

		// then
		assertThat(response.roomHistory()).hasSize(3);

		assertThat(response.roomHistory().get(0).deletedAt()).isNull();
		assertThat(response.roomHistory().get(0).title()).isEqualTo(room1.getTitle());

		assertThat(response.roomHistory().get(1).deletedAt()).isNull();
		assertThat(response.roomHistory().get(1).title()).isEqualTo(room2.getTitle());

		assertThat(response.roomHistory().get(2).deletedAt()).isNotNull();
		assertThat(response.roomHistory().get(2).title()).isEqualTo(participant3.getDeletedRoomTitle());
	}
}
