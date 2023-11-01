package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthorizationServerRequestServiceTest {

	@InjectMocks
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	String uri = "https://authorization/url?"
		+ "response_type=code&"
		+ "client_id=testtestetsttest&"
		+ "redirect_uri=http://redirect/url&scope=profile_nickname,profile_image";

	@DisplayName("로그인 페이지 접근 요청 성공")
	@Test
	void authorization_code_uri_generate_success() throws IOException {
		// given
		MockHttpServletResponse mockHttpServletResponse = mockHttpServletResponse = new MockHttpServletResponse();

		// when
		oAuth2AuthorizationServerRequestService.loginRequest(mockHttpServletResponse, uri);

		// then
		assertThat(mockHttpServletResponse.getContentType())
			.isEqualTo(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
		assertThat(mockHttpServletResponse.getRedirectedUrl()).isEqualTo(uri);
	}

	@DisplayName("redirect 실패 테스트")
	@Test
	void redirect_fail_test() {
		// given
		HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

		try {
			doThrow(IOException.class).when(mockHttpServletResponse).sendRedirect(any(String.class));

			assertThatThrownBy(() -> {
				// When + Then
				oAuth2AuthorizationServerRequestService.loginRequest(mockHttpServletResponse, uri);
			}).isExactlyInstanceOf(BadRequestException.class)
				.hasMessage(ErrorMessage.REQUEST_FAILED.getMessage());
		} catch (Exception ignored) {

		}
	}
}
