package com.moabam.api.dto.room;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertifyRoomRequest {

	private Long routineId;
	private MultipartFile image;
}
