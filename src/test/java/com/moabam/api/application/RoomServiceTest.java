package com.moabam.api.application;

import static com.moabam.api.domain.room.RoomType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.application.room.mapper.RoomMapper;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@InjectMocks
	private RoomService roomService;

	@Mock
	private MemberService memberService;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private ParticipantRepository participantRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	@DisplayName("비밀번호 없는 방 생성 성공")
	@Test
	void create_room_no_password_success() {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		Room expectedRoom = RoomMapper.toRoomEntity(createRoomRequest);
		given(roomRepository.save(any(Room.class))).willReturn(expectedRoom);

		// when
		Long result = roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
		assertThat(result).isEqualTo(expectedRoom.getId());
		assertThat(expectedRoom.getPassword()).isNull();
	}

	@DisplayName("비밀번호 있는 방 생성 성공")
	@Test
	void create_room_with_password_success() {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", "1234", routines, MORNING, 10, 4);

		Room expectedRoom = RoomMapper.toRoomEntity(createRoomRequest);
		given(roomRepository.save(any(Room.class))).willReturn(expectedRoom);

		// when
		Long result = roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
		assertThat(result).isEqualTo(expectedRoom.getId());
		assertThat(expectedRoom.getPassword()).isEqualTo("1234");
	}

	@DisplayName("방장 위임 성공")
	@Test
	void room_manager_mandate_success() {
		// given
		Long managerId = 1L;
		Long memberId = 2L;

		Room room = spy(RoomFixture.room());
		given(room.getId()).willReturn(1L);

		Participant memberParticipant = RoomFixture.participant(room, memberId);
		Participant managerParticipant = RoomFixture.participant(room, managerId);
		managerParticipant.enableManager();

		given(participantSearchRepository.findOne(memberId, room.getId())).willReturn(
			Optional.of(memberParticipant));
		given(participantSearchRepository.findOne(managerId, room.getId())).willReturn(
			Optional.of(managerParticipant));

		// when
		roomService.mandateRoomManager(managerId, room.getId(), memberId);

		// then
		assertThat(managerParticipant.isManager()).isFalse();
		assertThat(memberParticipant.isManager()).isTrue();
	}
}
