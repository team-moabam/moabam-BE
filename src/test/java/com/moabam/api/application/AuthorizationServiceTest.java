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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationMapper;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenRequest;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.api.dto.LoginResponse;
import com.moabam.api.dto.PublicClaim;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.AuthorizationResponseFixture;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

	@InjectMocks
	AuthorizationService authorizationService;

	@Mock
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Mock
	MemberService memberService;

	@Mock
	JwtProviderService jwtProviderService;

	OAuthConfig oauthConfig;
	TokenConfig tokenConfig;
	AuthorizationService noPropertyService;
	OAuthConfig noOAuthConfig;

	@BeforeEach
	public void initParams() {
		tokenConfig = new TokenConfig(null, 100000, 150000,
			"testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttest");
		ReflectionTestUtils.setField(authorizationService, "tokenConfig", tokenConfig);

		oauthConfig = new OAuthConfig(
			new OAuthConfig.Provider("https://authorization/url", "http://redirect/url", "http://token/url",
				"http://tokenInfo/url"),
			new OAuthConfig.Client("provider", "testtestetsttest", "testtesttest", "authorization_code",
				List.of("profile_nickname", "profile_image"))
		);
		ReflectionTestUtils.setField(authorizationService, "oAuthConfig", oauthConfig);

		noOAuthConfig = new OAuthConfig(
			new OAuthConfig.Provider(null, null, null, null),
			new OAuthConfig.Client(null, null, null, null, null)
		);
		noPropertyService = new AuthorizationService(noOAuthConfig, tokenConfig,
			oAuth2AuthorizationServerRequestService,
			memberService, jwtProviderService);
	}

	@DisplayName("인가코드 URI 생성 매퍼 실패")
	@Test
	void authorization_code_request_mapping_fail() {
		// When + Then
		Assertions.assertThatThrownBy(() -> AuthorizationMapper.toAuthorizationCodeRequest(noOAuthConfig))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("인가코드 URI 생성 매퍼 성공")
	@Test
	void authorization_code_request_mapping_success() {
		// Given
		AuthorizationCodeRequest authorizationCodeRequest = AuthorizationMapper.toAuthorizationCodeRequest(oauthConfig);

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
		authorizationService.redirectToLoginPage(mockHttpServletResponse);

		// then
		verify(oAuth2AuthorizationServerRequestService).loginRequest(eq(mockHttpServletResponse), anyString());
	}

	@DisplayName("인가코드 반환 실패")
	@Test
	void authorization_grant_fail() {
		// Given
		AuthorizationCodeResponse authorizationCodeResponse = new AuthorizationCodeResponse(null, "error",
			"errorDescription", null);

		// When + Then
		assertThatThrownBy(() -> authorizationService.requestToken(authorizationCodeResponse))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.GRANT_FAILED.getMessage());
	}

	@DisplayName("인가코드 반환 성공")
	@Test
	void authorization_grant_success() {
		// Given
		AuthorizationCodeResponse authorizationCodeResponse = new AuthorizationCodeResponse("test", null,
			null, null);
		AuthorizationTokenResponse authorizationTokenResponse =
			AuthorizationResponseFixture.authorizationTokenResponse();

		// When
		when(oAuth2AuthorizationServerRequestService.requestAuthorizationServer(anyString(), any())).thenReturn(
			new ResponseEntity<>(authorizationTokenResponse, HttpStatus.OK));

		// When + Then
		assertThatNoException().isThrownBy(() -> authorizationService.requestToken(authorizationCodeResponse));
	}

	@DisplayName("토큰 요청 매퍼 실패 - code null")
	@Test
	void token_request_mapping_failBy_code() {
		// When + Then
		Assertions.assertThatThrownBy(() -> AuthorizationMapper.toAuthorizationTokenRequest(oauthConfig, null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("토큰 요청 매퍼 실패 - config 에러")
	@Test
	void token_request_mapping_failBy_config() {
		// When + Then
		Assertions.assertThatThrownBy(() -> AuthorizationMapper.toAuthorizationTokenRequest(noOAuthConfig, "Test"))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("토큰 요청 매퍼 성공")
	@Test
	void token_request_mapping_success() {
		// Given
		String code = "Test";
		AuthorizationTokenRequest authorizationTokenRequest = AuthorizationMapper.toAuthorizationTokenRequest(
			oauthConfig,
			code);

		// When + Then
		assertThat(authorizationTokenRequest).isNotNull();
		assertAll(
			() -> assertThat(authorizationTokenRequest.clientId()).isEqualTo(oauthConfig.client().clientId()),
			() -> assertThat(authorizationTokenRequest.redirectUri()).isEqualTo(oauthConfig.provider().redirectUri()),
			() -> assertThat(authorizationTokenRequest.code()).isEqualTo(code)
		);
	}

	@DisplayName("토큰 변경 성공")
	@Test
	void generate_token() {
		// Given
		AuthorizationTokenResponse tokenResponse = AuthorizationResponseFixture.authorizationTokenResponse();
		AuthorizationTokenInfoResponse tokenInfoResponse
			= AuthorizationResponseFixture.authorizationTokenInfoResponse();

		// When
		when(oAuth2AuthorizationServerRequestService.tokenInfoRequest(
			any(String.class),
			eq("Bearer " + tokenResponse.accessToken())))
			.thenReturn(new ResponseEntity<>(tokenInfoResponse, HttpStatus.OK));

		// Then
		assertThatNoException().isThrownBy(() -> authorizationService.requestTokenInfo(tokenResponse));
	}

	@DisplayName("회원 가입 및 로그인 성공 테스트")
	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void signUp_success(boolean isSignUp) {
		// given
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationResponseFixture.authorizationTokenInfoResponse();
		LoginResponse loginResponse = LoginResponse.builder()
			.publicClaim(PublicClaim.builder()
				.id(1L)
				.nickname("nickname")
				.build())
			.isSignUp(isSignUp)
			.build();

		willReturn(loginResponse).given(memberService).login(authorizationTokenInfoResponse);

		// when
		LoginResponse result =
			authorizationService.signUpOrLogin(httpServletResponse, authorizationTokenInfoResponse);

		// then
		assertThat(loginResponse).isEqualTo(result);

		Cookie tokenType = httpServletResponse.getCookie("token_type");
		assertThat(tokenType).isNotNull();
		assertThat(tokenType.getValue()).isEqualTo("Bearer");

		Cookie accessCookie = httpServletResponse.getCookie("access_token");
		assertThat(accessCookie).isNotNull();
		assertAll(
			() -> assertThat(accessCookie.getSecure()).isTrue(),
			() -> assertThat(accessCookie.isHttpOnly()).isTrue(),
			() -> assertThat(accessCookie.getPath()).isEqualTo("/")
		);

		Cookie refreshCookie = httpServletResponse.getCookie("refresh_token");
		assertThat(refreshCookie).isNotNull();
		assertAll(
			() -> assertThat(refreshCookie.getSecure()).isTrue(),
			() -> assertThat(refreshCookie.isHttpOnly()).isTrue(),
			() -> assertThat(refreshCookie.getPath()).isEqualTo("/")
		);
	}
}
