package com.moabam.global.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

class UrlSubstringParserTest {

	@DisplayName("UrlSubstringParser 성공적으로 parse 하는지")
	@ParameterizedTest
	@CsvSource({
		"https://image.moabam.com/certifications/20231108/1_asdfsdfxcv-4815vcx-asfd, 1",
		"https://image.moabam.com/certifications/20231108/5_fwjo39ug-fi2og90-fkw0d, 5"
	})
	void url_substring_parser_success(String url, Long result) {
		// given, when
		String parseUrl = UrlSubstringParser.parseUrl(url, "_");

		// then
		Assertions.assertThat(Long.parseLong(parseUrl)).isEqualTo(result);
	}

	@DisplayName("UrlSubstringParser 실패하면 예외 던지는지")
	@ParameterizedTest
	@CsvSource({
		"https:image.moabam.com.certifications.20231108.1_asdfsdfxcv-4815vcx-asfd",
		"https://image.moabam.com/certifications/20231108/5-fwjo39ug-fi2og90-fkw0d",
		"https://image.moabam.com/certifications/20231108/5_fwjo39ug-fi2og90-fkw0d/",
		"https://image.moabam.com/certifications/20231108/5-fwjo39ug-fi2og90-fkw0d_/"
	})
	void url_substring_parser_success(String url) {
		// given, when, then
		Assertions.assertThatThrownBy(() -> UrlSubstringParser.parseUrl(url, "_"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_REQUEST_URL.getMessage());
	}
}
