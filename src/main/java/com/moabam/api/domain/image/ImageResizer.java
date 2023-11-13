package com.moabam.api.domain.image;

import static com.moabam.global.common.util.GlobalConstant.DELIMITER;
import static com.moabam.global.error.model.ErrorMessage.S3_INVALID_IMAGE;
import static com.moabam.global.error.model.ErrorMessage.S3_INVALID_IMAGE_SIZE;
import static com.moabam.global.error.model.ErrorMessage.S3_RESIZE_ERROR;
import static java.util.Objects.requireNonNull;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import com.moabam.global.error.exception.BadRequestException;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ImageResizer {

	private static final int MAX_IMAGE_SIZE = 1024 * 1024 * 10;
	private static final String IMAGE_FORMAT_PREFIX = "image/";
	private static final int FORMAT_INDEX = 1;

	private final MultipartFile image;
	private final String fileName;
	private MultipartFile resizedImage;

	@Builder
	public ImageResizer(MultipartFile image, String fileName) {
		this.image = validate(image);
		this.fileName = fileName;
	}

	public MultipartFile validate(MultipartFile image) {
		if (isNotImage(image)) {
			throw new BadRequestException(S3_INVALID_IMAGE);
		}
		if (image.getSize() > MAX_IMAGE_SIZE) {
			throw new BadRequestException(S3_INVALID_IMAGE_SIZE);
		}

		return image;
	}

	private boolean isNotImage(MultipartFile image) {
		String contentType = requireNonNull(image.getContentType());

		return !contentType.startsWith(IMAGE_FORMAT_PREFIX);
	}

	public void resizeImageToFixedSize(ImageType imageType) {
		ImageSize imageSize = switch (imageType) {
			case PROFILE_IMAGE -> ImageSize.PROFILE_IMAGE;
			case CERTIFICATION -> ImageSize.CERTIFICATION_IMAGE;
			case DEFAULT -> ImageSize.CAGE;
		};

		BufferedImage bufferedImage = getBufferedImage();

		int width = imageSize.getWidth();
		int height = getResizedHeight(width, bufferedImage);
		BufferedImage scaledImage = resize(bufferedImage, width, height);

		byte[] bytes = toByteArray(scaledImage);
		this.resizedImage = toMultipartFile(bytes);
	}

	private int getResizedHeight(int width, BufferedImage bufferedImage) {
		double ratio = (double)width / bufferedImage.getWidth();

		return (int)(bufferedImage.getHeight() * ratio);
	}

	private BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = canvas.getGraphics();
		graphics.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		graphics.dispose();

		return canvas;
	}

	private byte[] toByteArray(final BufferedImage result) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(result, getFormat(), byteArrayOutputStream);

			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			log.error("이미지 리사이징 에러", e);
			throw new BadRequestException(S3_RESIZE_ERROR);
		}
	}

	private String getFormat() {
		return requireNonNull(image.getContentType()).split(DELIMITER)[FORMAT_INDEX];
	}

	private BufferedImage getBufferedImage() {
		try {
			return ImageIO.read(image.getInputStream());
		} catch (IOException e) {
			log.error("이미지 리사이징 에러", e);
			throw new BadRequestException(S3_RESIZE_ERROR);
		}
	}

	private ResizedImage toMultipartFile(byte[] bytes) {
		return ResizedImage.of(fileName, image.getContentType(), bytes);
	}
}
