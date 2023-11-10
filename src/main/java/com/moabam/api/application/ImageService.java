package com.moabam.api.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.domain.resizedimage.ImageName;
import com.moabam.api.domain.resizedimage.ImageResizer;
import com.moabam.api.domain.resizedimage.ImageType;
import com.moabam.api.infrastructure.s3.S3Manager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

	private final S3Manager s3Manager;

	@Transactional
	public List<String> uploadImages(List<MultipartFile> multipartFiles, ImageType imageType) {

		List<String> result = new ArrayList<>();

		List<ImageResizer> imageResizers = multipartFiles.stream()
			.map(multipartFile -> this.toImageResizer(multipartFile, imageType))
			.toList();

		imageResizers.forEach(resizer -> {
				resizer.resizeImageToFixedSize(imageType);
				result.add(s3Manager.uploadImage(resizer.getResizedImage().getName(), resizer.getResizedImage()));
			}
		);

		return result;
	}

	private ImageResizer toImageResizer(MultipartFile multipartFile, ImageType imageType) {
		ImageName imageName = ImageName.of(multipartFile, imageType);

		return ImageResizer.builder()
			.image(multipartFile)
			.fileName(imageName.getFileName())
			.build();
	}

	@Transactional
	public void deleteImage(String imageUrl) {
		s3Manager.deleteImage(imageUrl);
	}
}
