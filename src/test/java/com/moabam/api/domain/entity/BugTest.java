package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DisplayNameGenerator.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.BadRequestException;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class BugTest {

	@Test
	void 벌레_개수가_음수이면_예외가_발생한다() {
		assertThatThrownBy(() -> Bug.builder()
			.morningBug(10)
			.nightBug(10)
			.goldenBug(-10)
			.build()
		).isInstanceOf(BadRequestException.class)
			.hasMessage("벌레 개수는 0 이상이어야 합니다.");
	}
}
