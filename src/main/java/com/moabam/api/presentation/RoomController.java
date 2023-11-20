package com.moabam.api.presentation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.application.room.RoomCertificationService;
import com.moabam.api.application.room.RoomSearchService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.RoomDetailsResponse;
import com.moabam.api.dto.room.RoomsHistoryResponse;
import com.moabam.api.dto.room.SearchAllRoomsResponse;
import com.moabam.global.auth.annotation.CurrentMember;
import com.moabam.global.auth.model.AuthorizationMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

	private final RoomService roomService;
	private final RoomSearchService roomSearchService;
	private final RoomCertificationService roomCertificationService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long createRoom(@CurrentMember AuthorizationMember authorizationMember,
		@Valid @RequestBody CreateRoomRequest createRoomRequest) {

		return roomService.createRoom(authorizationMember.id(), authorizationMember.nickname(), createRoomRequest);
	}

	@PutMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyRoom(@CurrentMember AuthorizationMember authorizationMember,
		@Valid @RequestBody ModifyRoomRequest modifyRoomRequest, @PathVariable("roomId") Long roomId) {

		roomService.modifyRoom(authorizationMember.id(), roomId, modifyRoomRequest);
	}

	@PostMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void enterRoom(@CurrentMember AuthorizationMember authorizationMember, @PathVariable("roomId") Long roomId,
		@Valid @RequestBody EnterRoomRequest enterRoomRequest) {

		roomService.enterRoom(authorizationMember.id(), roomId, enterRoomRequest);
	}

	@DeleteMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void exitRoom(@CurrentMember AuthorizationMember authorizationMember, @PathVariable("roomId") Long roomId) {
		roomService.exitRoom(authorizationMember.id(), roomId);
	}

	@GetMapping("/{roomId}/{date}")
	@ResponseStatus(HttpStatus.OK)
	public RoomDetailsResponse getRoomDetails(@CurrentMember AuthorizationMember authorizationMember,
		@PathVariable("roomId") Long roomId, @PathVariable("date") LocalDate date) {

		return roomSearchService.getRoomDetails(authorizationMember.id(), roomId, date);
	}

	@PostMapping("/{roomId}/certification")
	@ResponseStatus(HttpStatus.CREATED)
	public void certifyRoom(@CurrentMember AuthorizationMember authorizationMember, @PathVariable("roomId") Long roomId,
		@RequestPart List<MultipartFile> multipartFiles) {

		roomCertificationService.certifyRoom(authorizationMember.id(), roomId, multipartFiles);
	}

	@PutMapping("/{roomId}/members/{memberId}/mandate")
	@ResponseStatus(HttpStatus.OK)
	public void mandateManager(@CurrentMember AuthorizationMember authorizationMember,
		@PathVariable("roomId") Long roomId, @PathVariable("memberId") Long memberId) {

		roomService.mandateRoomManager(authorizationMember.id(), roomId, memberId);
	}

	@DeleteMapping("/{roomId}/members/{memberId}")
	@ResponseStatus(HttpStatus.OK)
	public void deportParticipant(@CurrentMember AuthorizationMember authorizationMember,
		@PathVariable("roomId") Long roomId, @PathVariable("memberId") Long memberId) {

		roomService.deportParticipant(authorizationMember.id(), roomId, memberId);
	}

	@GetMapping("/my-join")
	@ResponseStatus(HttpStatus.OK)
	public MyRoomsResponse getMyRooms(@CurrentMember AuthorizationMember authorizationMember) {
		return roomSearchService.getMyRooms(authorizationMember.id());
	}

	@GetMapping("/join-history")
	@ResponseStatus(HttpStatus.OK)
	public RoomsHistoryResponse getJoinHistory(@CurrentMember AuthorizationMember authorizationMember) {
		return roomSearchService.getJoinHistory(authorizationMember.id());
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public SearchAllRoomsResponse searchAllRooms(@RequestParam(value = "type", required = false) RoomType roomType,
		@RequestParam(value = "roomId", required = false) Long roomId) {

		return roomSearchService.searchAllRooms(roomType, roomId);
	}
}
