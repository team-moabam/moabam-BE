package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.BadRequestException;

class BugTest {

	@DisplayName("벌레 개수가 음수이면 예외가 발생한다.")
	@Test
	void negative_bug_count_throws_exception() {
		assertThatThrownBy(() -> Bug.builder()
			.morningBug(10)
			.nightBug(10)
			.goldenBug(-10)
			.build()
		).isInstanceOf(BadRequestException.class)
			.hasMessage("벌레 개수는 0 이상이어야 합니다.");
	}
}
