package com.moabam.global.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.moabam.global.common.util.PathResolver;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class PathFilterTest {

	@InjectMocks
	PathFilter pathFilter;

	@Mock
	PathResolver pathResolver;

	@DisplayName("Authentication을 넘기기 위한 필터 설정")
	@ParameterizedTest
	@ValueSource(strings = {
		"GET", "POST", "PATCH", "DELETE"
	})
	void filter_pass_for_authentication(String method) throws ServletException, IOException {
		// given
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		httpServletRequest.setMethod(method);
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

		willReturn(Optional.of(PathResolver.Path.builder()
			.uri("/")
			.build()))
			.given(pathResolver).permitPathMatch(any());

		// when
		pathFilter.doFilterInternal(httpServletRequest, httpServletResponse, new MockFilterChain());

		// then
		assertThat(httpServletRequest.getAttribute("isPermit"))
			.isEqualTo(true);
	}

	@DisplayName("경로 허가 없다.")
	@Test
	void filter_with_no_permit() throws ServletException, IOException {
		// given
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

		willReturn(Optional.empty())
			.given(pathResolver)
			.permitPathMatch(any());

		// when
		pathFilter.doFilterInternal(httpServletRequest, httpServletResponse, new MockFilterChain());

		// then
		assertThat(httpServletRequest.getAttribute("isPermit")).isNull();
	}
}
