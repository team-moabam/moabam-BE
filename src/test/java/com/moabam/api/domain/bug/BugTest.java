package com.moabam.api.domain.bug;

import static com.moabam.support.fixture.BugFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.moabam.global.error.exception.BadRequestException;

class BugTest {

	@DisplayName("벌레 개수가 음수이면 예외가 발생한다.")
	@ParameterizedTest
	@CsvSource({
		"-10, 10, 10",
		"10, -10, 10",
		"10, 10, -10",
	})
	void validate_bug_count_exception(int morningBug, int nightBug, int goldenBug) {
		Bug.BugBuilder bugBuilder = Bug.builder()
			.morningBug(morningBug)
			.nightBug(nightBug)
			.goldenBug(goldenBug);

		assertThatThrownBy(bugBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("벌레 개수는 0 이상이어야 합니다.");
	}

	@DisplayName("벌레를 사용한다.")
	@Nested
	class Use {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Bug bug = bug();

			// when
			bug.use(BugType.MORNING, 5);

			// then
			assertThat(bug.getMorningBug()).isEqualTo(MORNING_BUG - 5);
		}

		@DisplayName("벌레 개수가 부족하면 사용할 수 없다.")
		@Test
		void not_enough_exception() {
			// given
			Bug bug = bug();

			// when, then
			assertThatThrownBy(() -> bug.use(BugType.MORNING, 50))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("보유한 벌레가 부족합니다.");
		}
	}

	@DisplayName("해당 벌레 타입의 개수를 증가한다.")
	@Test
	void increase_bug_success() {
		// given
		Bug bug = bug();

		// when
		bug.increase(BugType.MORNING, 5);
		bug.increase(BugType.NIGHT, 5);
		bug.increase(BugType.GOLDEN, 5);

		// then
		assertThat(bug.getMorningBug()).isEqualTo(MORNING_BUG + 5);
		assertThat(bug.getNightBug()).isEqualTo(NIGHT_BUG + 5);
	}
}
