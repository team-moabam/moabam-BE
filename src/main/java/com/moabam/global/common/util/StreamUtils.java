package com.moabam.global.common.util;

import java.util.List;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StreamUtils {

	public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
		return list.stream()
			.map(mapper)
			.toList();
	}
}
