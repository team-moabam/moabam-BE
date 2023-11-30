package com.moabam.api.dto.room;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;

@Builder
public record CertifyRoomRequest(
	Long routineId,
	MultipartFile image
) {

}
