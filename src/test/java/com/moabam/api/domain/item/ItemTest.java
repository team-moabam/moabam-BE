package com.moabam.api.domain.item;

import static com.moabam.support.fixture.ItemFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.moabam.api.domain.bug.BugType;
import com.moabam.global.error.exception.BadRequestException;

class ItemTest {

	@DisplayName("아이템을 생성한다.")
	@Nested
	class Create {

		@DisplayName("해금 레벨은 기본 1로 설정한다.")
		@Test
		void default_unlock_level() {
			// given, when
			Item item = nightMageSkin();

			// then
			assertThat(item.getUnlockLevel()).isEqualTo(1);
		}

		@DisplayName("가격이 음수이면 예외가 발생한다.")
		@ParameterizedTest
		@CsvSource({
			"-10, 10",
			"10, -10",
		})
		void price_exception(int bugPrice, int goldenBugPrice) {
			Item.ItemBuilder itemBuilder = morningSantaSkin()
				.bugPrice(bugPrice)
				.goldenBugPrice(goldenBugPrice);

			assertThatThrownBy(itemBuilder::build)
				.isInstanceOf(BadRequestException.class)
				.hasMessage("가격은 0 이상이어야 합니다.");
		}

		@DisplayName("레벨이 1보다 작으면 예외가 발생한다.")
		@Test
		void level_exception() {
			Item.ItemBuilder itemBuilder = morningSantaSkin()
				.unlockLevel(-1);

			assertThatThrownBy(itemBuilder::build)
				.isInstanceOf(BadRequestException.class)
				.hasMessage("레벨은 1 이상이어야 합니다.");
		}
	}

	@DisplayName("해당 벌레 타입의 가격을 조회한다.")
	@ParameterizedTest
	@CsvSource({
		"MORNING, 10",
		"GOLDEN, 5",
	})
	void get_price_success(BugType bugType, int expected) {
		// given
		Item item = morningSantaSkin()
			.bugPrice(10)
			.goldenBugPrice(5)
			.build();

		// when, then
		assertThat(item.getPrice(bugType)).isEqualTo(expected);
	}

	@DisplayName("아이템 구매 가능 여부를 검증한다.")
	@Nested
	class ValidatePurchasable {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Item item = nightMageSkin();

			// when, then
			assertDoesNotThrow(() -> item.validatePurchasable(BugType.NIGHT, 5));
		}

		@DisplayName("해금 레벨이 높으면 구매할 수 없다.")
		@Test
		void unlocked_exception() {
			// given
			Item item = morningSantaSkin()
				.unlockLevel(10)
				.build();

			// when, then
			assertThatThrownBy(() -> item.validatePurchasable(BugType.MORNING, 5))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("아이템 해금 레벨이 높습니다.");
		}

		@DisplayName("벌레 타입이 맞지 않으면 구매할 수 없다.")
		@Test
		void bug_type_exception() {
			// given
			Item item = nightMageSkin();

			// when, then
			assertThatThrownBy(() -> item.validatePurchasable(BugType.MORNING, 5))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("해당 벌레 타입으로는 구매할 수 없는 아이템입니다.");
		}
	}
}
