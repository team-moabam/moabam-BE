package com.moabam.api.domain.item;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage {

	@Column(name = "awake_image", nullable = false)
	private String awake;

	@Column(name = "sleep_image", nullable = false)
	private String sleep;

	@Builder
	public ItemImage(String awakeImage, String sleepImage) {
		this.awake = requireNonNull(awakeImage);
		this.sleep = requireNonNull(sleepImage);
	}
}
