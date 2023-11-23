package com.moabam.api.dto.room;

import java.util.List;

import lombok.Builder;

@Builder
public record GetAllRoomsResponse(
	boolean hasNext,
	List<GetAllRoomResponse> rooms
) {

}
