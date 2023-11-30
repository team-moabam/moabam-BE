package com.moabam.api.application.image;

import static com.moabam.global.error.model.ErrorMessage.IMAGE_CONVERT_FAIL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.domain.image.ImageName;
import com.moabam.api.domain.image.ImageResizer;
import com.moabam.api.domain.image.ImageType;
import com.moabam.api.domain.image.NewImage;
import com.moabam.api.dto.room.CertifyRoomsRequest;
import com.moabam.api.infrastructure.s3.S3Manager;
import com.moabam.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

	private final S3Manager s3Manager;

	@Transactional
	public List<String> uploadImages(List<? extends MultipartFile> multipartFiles, ImageType imageType) {

		List<String> result = new ArrayList<>();

		List<ImageResizer> imageResizers = multipartFiles.stream()
			.map(multipartFile -> this.toImageResizer(multipartFile, imageType))
			.toList();

		imageResizers.forEach(resizer -> {
			resizer.resizeImageToFixedSize(imageType);
			result.add(s3Manager.uploadImage(resizer.getResizedImage().getName(), resizer.getResizedImage()));
		});

		return result;
	}

	public List<NewImage> getNewImages(CertifyRoomsRequest request) {
		return request.getCertifyRoomsRequest().stream()
			.map(certifyRoomRequest -> {
				try {
					return NewImage.of(String.valueOf(certifyRoomRequest.getRoutineId()),
						certifyRoomRequest.getImage().getContentType(), certifyRoomRequest.getImage().getBytes());
				} catch (IOException e) {
					throw new BadRequestException(IMAGE_CONVERT_FAIL);
				}
			})
			.toList();
	}

	@Transactional
	public void deleteImage(String imageUrl) {
		s3Manager.deleteImage(imageUrl);
	}

	private ImageResizer toImageResizer(MultipartFile multipartFile, ImageType imageType) {
		ImageName imageName = ImageName.of(multipartFile, imageType);

		return ImageResizer.builder()
			.image(multipartFile)
			.fileName(imageName.getFileName())
			.build();
	}
}
