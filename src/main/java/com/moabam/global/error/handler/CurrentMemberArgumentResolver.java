package com.moabam.global.error.handler;

import static com.moabam.global.common.util.AuthorizationThreadLocal.*;

import java.util.Objects;

import javax.annotation.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.moabam.api.dto.AuthorizationMember;
import com.moabam.global.common.annotation.CurrentMember;

public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Objects.nonNull(parameter.getParameterAnnotation(CurrentMember.class))
			&& parameter.getParameterType().isInstance(AuthorizationMember.class);
	}

	@Override
	public Object resolveArgument(@Nullable MethodParameter parameter, ModelAndViewContainer mavContainer,
		@Nullable NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		return getAuthorizationMember();
	}
}
