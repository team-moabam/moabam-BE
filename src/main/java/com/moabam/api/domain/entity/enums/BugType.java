package com.moabam.api.domain.entity.enums;

public enum BugType {

	MORNING,
	NIGHT,
	GOLDEN;

	public boolean isGoldenBug() {
		return this == GOLDEN;
	}
}
