package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.EnterRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.api.dto.RoomMapper;
import com.moabam.global.error.exception.BadRequestException;
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
	private final ParticipantSearchRepository participantSearchRepository;
	private final MemberService memberService;

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
		Participant participant = getParticipant(memberId, roomId);

		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}

		Room room = getRoom(roomId);
		room.changeTitle(modifyRoomRequest.title());
		room.changePassword(modifyRoomRequest.password());
		room.changeCertifyTime(modifyRoomRequest.certifyTime());
		room.changeMaxCount(modifyRoomRequest.maxUserCount());
	}

	@Transactional
	public void enterRoom(Long memberId, Long roomId, EnterRoomRequest enterRoomRequest) {
		Room room = getRoom(roomId);
		validateRoomEnter(memberId, enterRoomRequest.password(), room);

		room.increaseCurrentUserCount();
		memberService.increaseRoomCount(memberId, room.getRoomType());
		
		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
		participantRepository.save(participant);
	}

	@Transactional
	public void exitRoom(Long memberId, Long roomId) {
		Participant participant = getParticipant(memberId, roomId);
		Room room = participant.getRoom();

		if (participant.isManager() && room.getCurrentUserCount() != 1) {
			throw new BadRequestException(ROOM_EXIT_MANAGER_FAIL);
		}

		// TODO: 사용자의 방 입장 횟수 감소 기능 넣기
		participant.removeRoom();
		participantRepository.flush();
		participantRepository.delete(participant);

		if (!participant.isManager()) {
			room.decreaseCurrentUserCount();
			return;
		}

		roomRepository.flush();
		roomRepository.delete(room);
	}

	private Participant getParticipant(Long memberId, Long roomId) {
		return participantSearchRepository.findParticipant(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
	}

	private Room getRoom(Long roomId) {
		return roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));
	}

	private void validateRoomEnter(Long memberId, String requestPassword, Room room) {
		if (!memberService.isEnterRoomAvailable(memberId, room.getRoomType())) {
			throw new BadRequestException(MEMBER_ROOM_EXCEED);
		}

		if (!StringUtils.isEmpty(requestPassword) && !room.getPassword().equals(requestPassword)) {
			throw new BadRequestException(WRONG_ROOM_PASSWORD);
		}

		if (room.getCurrentUserCount() == room.getMaxUserCount()) {
			throw new BadRequestException(ROOM_MAX_USER_REACHED);
		}
	}
}
