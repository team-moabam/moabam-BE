package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.api.dto.RoomMapper;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

	private final RoomRepository roomRepository;
	private final RoutineRepository routineRepository;
	private final ParticipantRepository participantRepository;

	@Transactional
	public void createRoom(Long memberId, CreateRoomRequest createRoomRequest) {
		Room room = RoomMapper.toRoomEntity(createRoomRequest);
		List<Routine> routines = RoomMapper.toRoutineEntity(room, createRoomRequest.routines());
		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
		participant.enableManager();

		roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);
	}

	@Transactional
	public void modifyRoom(Long memberId, Long roomId, ModifyRoomRequest modifyRoomRequest) {
		// TODO: 추후에 별도 메서드로 뺄듯
		Participant participant = participantRepository.findParticipantByRoomIdAndMemberId(roomId, memberId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));

		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}

		Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));
		room.changeTitle(modifyRoomRequest.title());
		room.changePassword(modifyRoomRequest.password());
		room.changeCertifyTime(modifyRoomRequest.certifyTime());
		room.changeMaxCount(modifyRoomRequest.maxUserCount());
	}
}
