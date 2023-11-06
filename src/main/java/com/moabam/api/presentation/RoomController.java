package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.RoomService;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.EnterRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.api.dto.RoomDetailsResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

	private final RoomService roomService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
		roomService.createRoom(1L, createRoomRequest);
	}

	@PutMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyRoom(@Valid @RequestBody ModifyRoomRequest modifyRoomRequest,
		@PathVariable("roomId") Long roomId) {
		roomService.modifyRoom(1L, roomId, modifyRoomRequest);
	}

	@PostMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void enterRoom(@Valid @RequestBody EnterRoomRequest enterRoomRequest, @PathVariable("roomId") Long roomId) {
		roomService.enterRoom(1L, roomId, enterRoomRequest);
	}

	@DeleteMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void exitRoom(@PathVariable("roomId") Long roomId) {
		roomService.exitRoom(1L, roomId);
	}

	@GetMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public RoomDetailsResponse getRoomDetails(@PathVariable("roomId") Long roomId) {
		return roomService.getRoomDetails(1L, roomId);
	}
}
