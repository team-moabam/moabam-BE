package com.moabam.support.common;

import static java.util.Objects.*;

import java.lang.reflect.AnnotatedElement;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.support.annotation.WithMember;

public class FilterProcessExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

	@Override
	public void beforeEach(ExtensionContext context) {
		AnnotatedElement annotatedElement =
			context.getElement().orElse(null);

		if (isNull(annotatedElement)) {
			return;
		}

		WithMember withMember = annotatedElement.getAnnotation(WithMember.class);

		if (isNull(withMember)) {
			return;
		}

		AuthorizationThreadLocal.setAuthorizationMember(
			new AuthorizationMember(withMember.id(), withMember.nickname(), withMember.role()));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		AuthorizationThreadLocal.remove();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
		ParameterResolutionException {
		return parameterContext.getParameter().isAnnotationPresent(WithMember.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
		ParameterResolutionException {
		WithMember withMember = parameterContext.getParameter().getAnnotation(WithMember.class);

		if (isNull(withMember)) {
			return null;
		}

		return new AuthorizationMember(withMember.id(), withMember.nickname(), withMember.role());
	}
}
