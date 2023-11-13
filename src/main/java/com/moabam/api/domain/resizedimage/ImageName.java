package com.moabam.api.domain.resizedimage;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageName {

	private static final String CERTIFICATION_PATH = "certifications" + DELIMITER + LocalDate.now() + DELIMITER;
	private static final String PROFILE_IMAGE = "members/profile" + DELIMITER;
	private static final String DEFAULT = "moabam/default" + DELIMITER;

	private final String fileName;

	public static ImageName of(MultipartFile file, ImageType imageType) {
		return switch (imageType) {
			case CERTIFICATION -> new ImageName(CERTIFICATION_PATH + file.getName() + "_" + UUID.randomUUID());
			case PROFILE_IMAGE -> new ImageName(PROFILE_IMAGE + file.getName() + "_" + UUID.randomUUID());
			case DEFAULT -> new ImageName(DEFAULT + file.getName());
		};
	}
}
