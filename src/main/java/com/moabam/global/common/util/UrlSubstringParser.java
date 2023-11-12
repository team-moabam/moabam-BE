package com.moabam.global.common.util;

import static com.moabam.global.common.util.GlobalConstant.*;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlSubstringParser {

	public static String parseUrl(String url, String distinctToken) {

		int lastSlashTokenIndex = url.lastIndexOf(DELIMITER);
		int distinctTokenIndex = url.indexOf(distinctToken);

		if (lastSlashTokenIndex == -1 || distinctTokenIndex == -1 || lastSlashTokenIndex > distinctTokenIndex) {
			throw new BadRequestException(ErrorMessage.INVALID_REQUEST_URL);
		}

		return url.substring(lastSlashTokenIndex + 1, distinctTokenIndex);
	}
}
