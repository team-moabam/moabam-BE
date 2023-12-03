package com.moabam.global.common.util;

import java.util.Objects;
import java.util.function.Function;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicQuery {

	public static <T> BooleanExpression generateEq(T value, Function<T, BooleanExpression> function) {
		if (Objects.isNull(value)) {
			return null;
		}

		return function.apply(value);
	}

	public static <T extends SimpleExpression> BooleanExpression generateIsNull(Boolean value, T field) {
		if (Objects.isNull(value)) {
			return null;
		}

		if (Boolean.TRUE.equals(value)) {
			return field.isNull();
		}

		return field.isNotNull();
	}
}
