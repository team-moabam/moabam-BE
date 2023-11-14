package com.moabam.api.domain.bug;

public enum BugType {

	MORNING,
	NIGHT,
	GOLDEN;

	public boolean isGoldenBug() {
		return this == GOLDEN;
	}
}
