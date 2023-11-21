package com.moabam.global.auth.handler;

import static com.moabam.global.auth.model.AuthorizationThreadLocal.*;

import java.util.Objects;

import javax.annotation.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Objects.nonNull(parameter.getParameterAnnotation(Auth.class))
			&& parameter.getParameterType().equals(AuthMember.class);
	}

	@Override
	public Object resolveArgument(@Nullable MethodParameter parameter, ModelAndViewContainer mavContainer,
		@Nullable NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		return getAuthMember();
	}
}
