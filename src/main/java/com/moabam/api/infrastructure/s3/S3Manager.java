package com.moabam.api.infrastructure.s3;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.global.error.exception.BadRequestException;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3Manager {

	private final S3Template s3Template;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${spring.cloud.aws.s3.url}")
	private String s3BaseUrl;

	@Value("${spring.cloud.aws.cloud-front.url}")
	private String cloudFrontUrl;

	public String uploadImage(String key, MultipartFile file) {
		try {
			s3Template.upload(bucket, key, file.getInputStream(),
				ObjectMetadata.builder().contentType("image/png").build());

			return cloudFrontUrl + key;
		} catch (IOException e) {
			throw new BadRequestException(S3_UPLOAD_FAIL);
		}
	}

	public void deleteImage(String objectUrl) {
		String s3Url = objectUrl.replace(cloudFrontUrl, s3BaseUrl);
		s3Template.deleteObject(s3Url);
	}
}
