package com.moabam.api.application.room;

import static com.moabam.api.domain.room.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.mapper.ParticipantMapper;
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
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {

	private final RoomRepository roomRepository;
	private final RoutineRepository routineRepository;
	private final ParticipantRepository participantRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final MemberService memberService;

	@Transactional
	public Long createRoom(Long memberId, CreateRoomRequest createRoomRequest) {
		Room room = RoomMapper.toRoomEntity(createRoomRequest);
		List<Routine> routines = RoutineMapper.toRoutineEntities(room, createRoomRequest.routines());
		Participant participant = ParticipantMapper.toParticipant(room, memberId);

		validateEnteredRoomCount(memberId, room.getRoomType());

		Member member = memberService.findMember(memberId);
		member.enterRoom(room.getRoomType());
		participant.enableManager();
		room.changeManagerNickname(member.getNickname());

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

		List<Routine> routines = routineRepository.findAllByRoomId(roomId);
		routineRepository.deleteAll(routines);

		List<Routine> newRoutines = RoutineMapper.toRoutineEntities(room, modifyRoomRequest.routines());
		routineRepository.saveAll(newRoutines);
	}

	@Transactional
	public void enterRoom(Long memberId, Long roomId, EnterRoomRequest enterRoomRequest) {
		Room room = roomRepository.findWithPessimisticLockById(roomId).orElseThrow(
			() -> new NotFoundException(ROOM_NOT_FOUND));
		validateRoomEnter(memberId, enterRoomRequest.password(), room);

		Member member = memberService.findMember(memberId);
		member.enterRoom(room.getRoomType());
		room.increaseCurrentUserCount();

		Participant participant = ParticipantMapper.toParticipant(room, memberId);
		participantRepository.save(participant);
	}

	@Transactional
	public void exitRoom(Long memberId, Long roomId) {
		Participant participant = getParticipant(memberId, roomId);
		Room room = participant.getRoom();

		validateRoomExit(participant, room);

		Member member = memberService.findMember(memberId);
		member.exitRoom(room.getRoomType());

		participant.removeRoom();
		participantRepository.flush();
		participantRepository.delete(participant);

		if (!participant.isManager()) {
			room.decreaseCurrentUserCount();
			return;
		}

		List<Routine> routines = routineRepository.findAllByRoomId(roomId);
		routineRepository.deleteAll(routines);
		roomRepository.delete(room);
	}

	@Transactional
	public void mandateManager(Long managerId, Long roomId, Long memberId) {
		Participant managerParticipant = getParticipant(managerId, roomId);
		Participant memberParticipant = getParticipant(memberId, roomId);
		validateManagerAuthorization(managerParticipant);

		Room room = managerParticipant.getRoom();
		Member member = memberService.findMember(memberParticipant.getMemberId());
		room.changeManagerNickname(member.getNickname());

		managerParticipant.disableManager();
		memberParticipant.enableManager();
	}

	@Transactional
	public void deportParticipant(Long managerId, Long roomId, Long memberId) {
		validateDeportParticipant(managerId, memberId);
		Participant managerParticipant = getParticipant(managerId, roomId);
		Participant memberParticipant = getParticipant(memberId, roomId);
		validateManagerAuthorization(managerParticipant);

		Room room = managerParticipant.getRoom();
		memberParticipant.removeRoom();
		participantRepository.delete(memberParticipant);
		room.decreaseCurrentUserCount();

		Member member = memberService.findMember(memberId);
		member.exitRoom(room.getRoomType());
	}

	public boolean checkIfParticipant(Long memberId, Long roomId) {
		try {
			getParticipant(memberId, roomId);
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}

	public void validateRoomById(Long roomId) {
		if (!roomRepository.existsById(roomId)) {
			throw new NotFoundException(ROOM_NOT_FOUND);
		}
	}

	public Room findRoom(Long roomId) {
		return roomRepository.findById(roomId)
			.orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));
	}

	private Participant getParticipant(Long memberId, Long roomId) {
		return participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
	}

	private void validateDeportParticipant(Long managerId, Long memberId) {
		if (managerId.equals(memberId)) {
			throw new BadRequestException(PARTICIPANT_DEPORT_ERROR);
		}
	}

	private void validateManagerAuthorization(Participant participant) {
		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}
	}

	private void validateRoomEnter(Long memberId, String requestPassword, Room room) {
		validateEnteredRoomCount(memberId, room.getRoomType());

		if (!StringUtils.isEmpty(requestPassword) && !room.getPassword().equals(requestPassword)) {
			throw new BadRequestException(WRONG_ROOM_PASSWORD);
		}
		if (room.getCurrentUserCount() == room.getMaxUserCount()) {
			throw new BadRequestException(ROOM_MAX_USER_REACHED);
		}
	}

	private void validateEnteredRoomCount(Long memberId, RoomType roomType) {
		Member member = memberService.findMember(memberId);

		if (roomType.equals(MORNING) && member.getCurrentMorningCount() >= 3) {
			throw new BadRequestException(MEMBER_ROOM_EXCEED);
		}
		if (roomType.equals(NIGHT) && member.getCurrentNightCount() >= 3) {
			throw new BadRequestException(MEMBER_ROOM_EXCEED);
		}
	}

	private void validateRoomExit(Participant participant, Room room) {
		if (participant.isManager() && room.getCurrentUserCount() != 1) {
			throw new BadRequestException(ROOM_EXIT_MANAGER_FAIL);
		}
	}
}
