package com.moabam.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.application.room.RoomService;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.EnterRoomRequest;
import com.moabam.api.dto.room.ModifyRoomRequest;
import com.moabam.api.dto.room.RoomDetailsResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

	private final RoomService roomService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
		return roomService.createRoom(1L, createRoomRequest);
	}

	@PutMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyRoom(@Valid @RequestBody ModifyRoomRequest modifyRoomRequest,
		@PathVariable("roomId") Long roomId) {
		roomService.modifyRoom(1L, roomId, modifyRoomRequest);
	}

	@PostMapping("/{roomId}")
	@ResponseStatus(HttpStatus.OK)
	public void enterRoom(@PathVariable("roomId") Long roomId, @Valid @RequestBody EnterRoomRequest enterRoomRequest) {
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

	@PostMapping("/{roomId}/certification")
	@ResponseStatus(HttpStatus.CREATED)
	public void certifyRoom(@PathVariable("roomId") Long roomId, @RequestPart List<MultipartFile> multipartFiles) {
		roomService.certifyRoom(1L, roomId, multipartFiles);
	}
}
