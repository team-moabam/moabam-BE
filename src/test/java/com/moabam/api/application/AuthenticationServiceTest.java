package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;
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
			new OAuthConfig.Provider("https://authorization/url", "http://redirect/url", "http://token/url"),
			new OAuthConfig.Client("provider", "testtestetsttest", "testtesttest", "authorization_code",
				List.of("profile_nickname", "profile_image"))
		);
		ReflectionTestUtils.setField(authenticationService, "oAuthConfig", oauthConfig);

		noOAuthConfig = new OAuthConfig(
			new OAuthConfig.Provider(null, null, null),
			new OAuthConfig.Client(null, null, null, null, null)
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
				.hasMessage(ErrorMessage.REQUEST_FAILED.getMessage());
		} catch (Exception ignored) {

		}
	}

	@DisplayName("인가코드 반환 실패 테스트")
	@Test
	void authorization_grant_fail_test() {
		// Given
		AuthorizationCodeResponse authorizationCodeResponse = new AuthorizationCodeResponse(null, "error",
			"errorDescription", null);

		// When + Then
		assertThatThrownBy(() -> authenticationService.requestToken(authorizationCodeResponse))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.GRANT_FAILED.getMessage());
	}

	@DisplayName("토큰 발급 실패")
	@ParameterizedTest
	@ValueSource(ints = {400, 401, 403, 429, 500, 502, 503})
	void token_issue_fail(int code) {
		// given
		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse = mock(ResponseEntity.class);
		AuthorizationCodeResponse authorizationCodeResponse = new AuthorizationCodeResponse(
			"testtesttesttesttesttest", null,
			null, null);

		given(new RestTemplate().exchange(
			any(String.class), HttpMethod.POST, any(HttpEntity.class), AuthorizationTokenResponse.class
		)).willReturn(authorizationTokenResponse);

		// When + Then
		when(authorizationTokenResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(code));

		assertThatThrownBy(() -> authenticationService.requestToken(authorizationCodeResponse))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.REQUEST_FAILED.getMessage());
	}
}
