package com.moabam.global.common.handler;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.enums.Role;
import com.moabam.api.dto.AuthorizationMember;
import com.moabam.global.common.annotation.CurrentMember;
import com.moabam.global.common.util.AuthorizationThreadLocal;

@ExtendWith(MockitoExtension.class)
class CurrentMemberArgumentResolverTest {

	@InjectMocks
	CurrentMemberArgumentResolver currentMemberArgumentResolver;

	@Nested
	@DisplayName("제공 파라미터 검증")
	class supportParameter {

		@DisplayName("파라미터 제공 성공")
		@Test
		void support_parameter_success() {
			// given
			MethodParameter parameter = mock(MethodParameter.class);

			willReturn(mock(CurrentMember.class))
				.given(parameter).getParameterAnnotation(any());
			willReturn(AuthorizationMember.class)
				.given(parameter).getParameterType();

			// when
			boolean support = currentMemberArgumentResolver.supportsParameter(parameter);

			// then
			assertThat(support).isTrue();
		}

		@DisplayName("어노테이션이 없어서 지원 실패")
		@Test
		void support_paramter_failby_no_annotation() {
			// given
			MethodParameter parameter = mock(MethodParameter.class);

			willReturn(null)
				.given(parameter).getParameterAnnotation(any());

			// when
			boolean support = currentMemberArgumentResolver.supportsParameter(parameter);

			// then
			assertThat(support).isFalse();
		}

		@DisplayName("AuthorizationMember 클래스로 받지 않았을 때 실패")
		@Test
		void support_paramter_failby_not_authorizationmember() {
			// given
			MethodParameter parameter = mock(MethodParameter.class);

			willReturn(mock(CurrentMember.class))
				.given(parameter).getParameterAnnotation(any());
			willReturn(Member.class)
				.given(parameter).getParameterType();

			// when
			boolean support = currentMemberArgumentResolver.supportsParameter(parameter);

			// then
			assertThat(support).isFalse();
		}
	}

	@DisplayName("값 변환한다")
	@Nested
	class Resolve {

		@DisplayName("값 변환 성공")
		@Test
		void resolve_argument_success() {
			MethodParameter parameter = mock(MethodParameter.class);
			ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
			NativeWebRequest webRequest = mock(NativeWebRequest.class);
			WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

			AuthorizationThreadLocal.setAuthorizationMember(new AuthorizationMember(1L, "park", Role.USER));

			Object object =
				currentMemberArgumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

			assertAll(
				() -> assertThat(object).isNotNull(),
				() -> {
					AuthorizationMember authorizationMember = (AuthorizationMember)object;

					assertThat(authorizationMember.id()).isEqualTo(1L);
				}
			);
		}
	}

}
