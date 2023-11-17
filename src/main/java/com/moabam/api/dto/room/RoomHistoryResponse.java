package com.moabam.api.dto.room;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record RoomHistoryResponse(
	Long roomId,
	String title,
	LocalDateTime createdAt,
	LocalDateTime deletedAt
) {

}
