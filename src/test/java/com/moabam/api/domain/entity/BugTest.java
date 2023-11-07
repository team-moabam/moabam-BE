package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
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
		Wallet.WalletBuilder walletBuilder = Wallet.builder()
			.morningBug(morningBug)
			.nightBug(nightBug)
			.goldenBug(goldenBug);

		assertThatThrownBy(walletBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("벌레 개수는 0 이상이어야 합니다.");
	}
}
