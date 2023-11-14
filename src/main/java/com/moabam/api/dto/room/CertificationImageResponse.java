package com.moabam.api.dto.room;

import lombok.Builder;

@Builder
public record CertificationImageResponse(
	Long routineId,
	String image
) {

}
