package com.moabam.api.application;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.repository.CertificationRepository;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.dto.CreateRoomRequest;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@InjectMocks
	private RoomService roomService;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private CertificationRepository certificationRepository;

	@Mock
	private ParticipantRepository participantRepository;

	@DisplayName("비밀번호 없는 방 생성 성공")
	@Test
	void create_room_no_password_success() {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		// when
		roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
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

		// when
		roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
	}
}
