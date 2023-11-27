package com.moabam.api.domain.bug;

import static com.moabam.global.error.model.ErrorMessage.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bug {

	@Column(name = "morning_bug", nullable = false)
	@ColumnDefault("0")
	private int morningBug;

	@Column(name = "night_bug", nullable = false)
	@ColumnDefault("0")
	private int nightBug;

	@Column(name = "golden_bug", nullable = false)
	@ColumnDefault("0")
	private int goldenBug;

	@Builder
	private Bug(int morningBug, int nightBug, int goldenBug) {
		this.morningBug = validateBugCount(morningBug);
		this.nightBug = validateBugCount(nightBug);
		this.goldenBug = validateBugCount(goldenBug);
	}

	private int validateBugCount(int bug) {
		if (bug < 0) {
			throw new BadRequestException(INVALID_BUG_COUNT);
		}

		return bug;
	}

	public void use(BugType bugType, int price) {
		int currentBug = getBug(bugType);
		validateEnoughBug(currentBug, price);
		decrease(bugType, price);
	}

	private int getBug(BugType bugType) {
		return switch (bugType) {
			case MORNING -> this.morningBug;
			case NIGHT -> this.nightBug;
			case GOLDEN -> this.goldenBug;
		};
	}

	private void validateEnoughBug(int currentBug, int price) {
		if (price > currentBug) {
			throw new BadRequestException(BUG_NOT_ENOUGH);
		}
	}

	private void decrease(BugType bugType, int bug) {
		switch (bugType) {
			case MORNING -> this.morningBug -= bug;
			case NIGHT -> this.nightBug -= bug;
			case GOLDEN -> this.goldenBug -= bug;
		}
	}

	public void increase(BugType bugType, int bug) {
		switch (bugType) {
			case MORNING -> this.morningBug += bug;
			case NIGHT -> this.nightBug += bug;
			case GOLDEN -> this.goldenBug += bug;
		}

		if (bugType.equals(BugType.NIGHT)) {
			this.nightBug += bug;
		}
	}

	public void charge(int quantity) {
		this.goldenBug += quantity;
	}
}
