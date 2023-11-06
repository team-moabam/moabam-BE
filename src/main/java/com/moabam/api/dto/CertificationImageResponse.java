package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record CertificationImageResponse(
	Long routineId,
	String image
) {

}
