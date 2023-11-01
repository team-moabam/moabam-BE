package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.OAuthMapper;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

	@InjectMocks
	AuthenticationService authenticationService;

	@Mock
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

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
		noPropertyService = new AuthenticationService(noOAuthConfig, oAuth2AuthorizationServerRequestService);

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

	@DisplayName("redirect 로그인페이지 성공")
	@Test
	void redirect_loginPage_success() {
		// given
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

		// when
		authenticationService.redirectToLoginPage(mockHttpServletResponse);

		// then
		verify(oAuth2AuthorizationServerRequestService).loginRequest(eq(mockHttpServletResponse), anyString());
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
}
