package com.moabam.api.application.room;

import static com.moabam.api.domain.room.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.mapper.RoomMapper;
import com.moabam.api.application.room.mapper.RoutineMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.domain.room.repository.RoutineSearchRepository;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
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
	private final RoutineSearchRepository routineSearchRepository;
	private final ParticipantRepository participantRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final MemberService memberService;

	@Transactional
	public Long createRoom(Long memberId, CreateRoomRequest createRoomRequest) {
		Room room = RoomMapper.toRoomEntity(createRoomRequest);
		List<Routine> routines = RoutineMapper.toRoutineEntities(room, createRoomRequest.routines());
		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();

		participant.enableManager();
		Room savedRoom = roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);

		return savedRoom.getId();
	}

	@Transactional
	public void modifyRoom(Long memberId, Long roomId, ModifyRoomRequest modifyRoomRequest) {
		Participant participant = getParticipant(memberId, roomId);
		validateManagerAuthorization(participant);

		Room room = participant.getRoom();
		room.changeTitle(modifyRoomRequest.title());
		room.changeAnnouncement(modifyRoomRequest.announcement());
		room.changePassword(modifyRoomRequest.password());
		room.changeCertifyTime(modifyRoomRequest.certifyTime());
		room.changeMaxCount(modifyRoomRequest.maxUserCount());

		List<Routine> routines = routineSearchRepository.findByRoomId(roomId);
		routineRepository.deleteAll(routines);

		List<Routine> newRoutines = RoutineMapper.toRoutineEntities(room, modifyRoomRequest.routines());
		routineRepository.saveAll(newRoutines);
	}

	@Transactional
	public void enterRoom(Long memberId, Long roomId, EnterRoomRequest enterRoomRequest) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));
		validateRoomEnter(memberId, enterRoomRequest.password(), room);

		room.increaseCurrentUserCount();
		increaseRoomCount(memberId, room.getRoomType());

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

		decreaseRoomCount(memberId, room.getRoomType());
		participant.removeRoom();
		participantRepository.delete(participant);

		if (!participant.isManager()) {
			room.decreaseCurrentUserCount();
			return;
		}

		roomRepository.flush();
		roomRepository.delete(room);
	}

	@Transactional
	public void mandateRoomManager(Long managerId, Long roomId, Long memberId) {
		Participant managerParticipant = getParticipant(managerId, roomId);
		Participant memberParticipant = getParticipant(memberId, roomId);
		validateManagerAuthorization(managerParticipant);

		managerParticipant.disableManager();
		memberParticipant.enableManager();
	}

	public void validateRoomById(Long roomId) {
		if (!roomRepository.existsById(roomId)) {
			throw new NotFoundException(ROOM_NOT_FOUND);
		}
	}

	private Participant getParticipant(Long memberId, Long roomId) {
		return participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
	}

	private void validateManagerAuthorization(Participant participant) {
		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}
	}

	private void validateRoomEnter(Long memberId, String requestPassword, Room room) {
		if (!isEnterRoomAvailable(memberId, room.getRoomType())) {
			throw new BadRequestException(MEMBER_ROOM_EXCEED);
		}
		if (!StringUtils.isEmpty(requestPassword) && !room.getPassword().equals(requestPassword)) {
			throw new BadRequestException(WRONG_ROOM_PASSWORD);
		}
		if (room.getCurrentUserCount() == room.getMaxUserCount()) {
			throw new BadRequestException(ROOM_MAX_USER_REACHED);
		}
	}

	private boolean isEnterRoomAvailable(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING) && member.getCurrentMorningCount() >= 3) {
			return false;
		}
		if (roomType.equals(NIGHT) && member.getCurrentNightCount() >= 3) {
			return false;
		}

		return true;
	}

	private void increaseRoomCount(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING)) {
			member.enterMorningRoom();
			return;
		}

		member.enterNightRoom();
	}

	private void decreaseRoomCount(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING)) {
			member.exitMorningRoom();
			return;
		}

		member.exitNightRoom();
	}
}
