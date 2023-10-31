package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.OAuthMapper;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

	@InjectMocks
	AuthenticationService authenticationService;
	OAuthConfig oauthConfig;
	AuthenticationService noPropertyService;
	OAuthConfig noOAuthConfig;

	@BeforeEach
	public void initParams() {
		oauthConfig = new OAuthConfig(
			new OAuthConfig.Provider("https://authorization/url", "http://redirect/url"),
			new OAuthConfig.Client("provider", "testtestetsttest", "authorization_code",
				List.of("profile_nickname", "profile_image"))
		);
		ReflectionTestUtils.setField(authenticationService, "oAuthConfig", oauthConfig);

		noOAuthConfig = new OAuthConfig(
			new OAuthConfig.Provider(null, null),
			new OAuthConfig.Client(null, null, null, null)
		);
		noPropertyService = new AuthenticationService(noOAuthConfig);

	}

	@DisplayName("인가코드 URI 생성 매퍼 실패")
	@Test
	void authorization_code_request_mapping_fail() {
		// When + Then
		Assertions.assertThatThrownBy(() -> OAuthMapper.toAuthorizationCodeRequest(noOAuthConfig))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("인가코드 URI 생성 매퍼 성공")
	@Test
	void authorization_code_request_mapping_success() {
		// Given
		AuthorizationCodeRequest authorizationCodeRequest = OAuthMapper.toAuthorizationCodeRequest(oauthConfig);

		// When + Then
		assertThat(authorizationCodeRequest).isNotNull();
		assertAll(
			() -> assertThat(authorizationCodeRequest.clientId()).isEqualTo(oauthConfig.client().clientId()),
			() -> assertThat(authorizationCodeRequest.redirectUri()).isEqualTo(oauthConfig.provider().redirectUri())
		);
	}

	@DisplayName("인가코드 URI 생성 성공")
	@Test
	void authorization_code_uri_generate_success() throws IOException {
		// given
		String uri = "https://authorization/url?"
			+ "response_type=code&"
			+ "client_id=testtestetsttest&"
			+ "redirect_uri=http://redirect/url&scope=profile_nickname,profile_image";

		MockHttpServletResponse mockHttpServletResponse = mockHttpServletResponse = new MockHttpServletResponse();

		// when
		authenticationService.redirectToLoginPage(mockHttpServletResponse);

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
				authenticationService.redirectToLoginPage(mockHttpServletResponse);
			}).isExactlyInstanceOf(BadRequestException.class)
				.hasMessage(ErrorMessage.REQUEST_FAILD.getMessage());
		} catch (Exception ignored) {

		}
	}
}
