package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.domain.resizedimage.ImageType;
import com.moabam.api.domain.resizedimage.ResizedImage;
import com.moabam.api.infrastructure.s3.S3Manager;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private S3Manager s3Manager;

	@DisplayName("이미지 리사이징 이후 업로드 성공")
	@Test
	void image_resize_upload_success() {
		// given
		List<MultipartFile> multipartFiles = new ArrayList<>();
		ImageType imageType = ImageType.CERTIFICATION;
		MockMultipartFile image1 = RoomFixture.makeMultipartFile1();
		List<MultipartFile> images = List.of(image1);

		given(s3Manager.uploadImage(anyString(), any(ResizedImage.class))).willReturn(image1.getName());

		// when
		List<String> result = imageService.uploadImages(images, imageType);

		// then
		assertThat(image1.getName()).isEqualTo(result.get(0));
	}

	@DisplayName("이미지 삭제 성공")
	@Test
	void delete_image_success() {
		// given
		String imageUrl = "test";

		// when
		imageService.deleteImage(imageUrl);

		// then
		verify(s3Manager).deleteImage(imageUrl);
	}
}
