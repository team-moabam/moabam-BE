package com.moabam.api.dto.room;

import java.util.List;

import lombok.Builder;

@Builder
public record CertificationImagesResponse(
	List<CertificationImageResponse> images
) {

}
