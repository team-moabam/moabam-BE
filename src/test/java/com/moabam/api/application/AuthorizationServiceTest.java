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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.auth.JwtProviderService;
import com.moabam.api.application.auth.OAuth2AuthorizationServerRequestService;
import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.dto.auth.AuthorizationCodeRequest;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenRequest;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.infrastructure.repository.auth.TokenRepository;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.common.util.CookieUtils;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.AuthorizationResponseFixture;
import com.moabam.support.fixture.TokenSaveValueFixture;

import jakarta.servlet.http.Cookie;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class AuthorizationServiceTest {

	@InjectMocks
	AuthorizationService authorizationService;

	@Mock
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Mock
	MemberService memberService;

	@Mock
	JwtProviderService jwtProviderService;

	@Mock
	TokenRepository tokenRepository;

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
			memberService, jwtProviderService, tokenRepository);
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

	@DisplayName("토큰 redis 검증")
	@Test
	void valid_token_in_redis() {
		// Given
		willReturn(TokenSaveValueFixture.tokenSaveValue("token"))
			.given(tokenRepository).getTokenSaveValue(1L);

		// When + Then
		assertThatNoException().isThrownBy(() ->
			authorizationService.validTokenPair(1L, "token"));
	}

	@DisplayName("이전 토큰과 동일한지 검증")
	@Test
	void valid_token_failby_notEquals_token() {
		// Given
		willReturn(TokenSaveValueFixture.tokenSaveValue("token"))
			.given(tokenRepository).getTokenSaveValue(1L);

		// When + Then
		assertThatThrownBy(() -> authorizationService.validTokenPair(1L, "oldToken"))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(ErrorMessage.AUTHENTICATE_FAIL.getMessage());
		verify(tokenRepository).delete(1L);
	}

	@DisplayName("토큰 삭제 성공")
	@Test
	void error_with_expire_token(@WithMember AuthorizationMember authorizationMember) {
		// given
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		httpServletRequest.setCookies(
			CookieUtils.tokenCookie("access_token", "value", 100000),
			CookieUtils.tokenCookie("refresh_token", "value", 100000),
			CookieUtils.typeCookie("Bearer", 100000)
		);

		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

		// When
		authorizationService.logout(authorizationMember, httpServletRequest, httpServletResponse);
		Cookie cookie = httpServletResponse.getCookie("access_token");

		// Then
		assertAll(
			() -> assertThat(cookie).isNotNull(),
			() -> assertThat(cookie.getMaxAge()).isZero(),
			() -> assertThat(cookie.getValue()).isEqualTo("value")
		);

		verify(tokenRepository).delete(authorizationMember.id());
	}

	@DisplayName("토큰 없어서 삭제 실패")
	@Test
	void token_null_delete_fail(@WithMember AuthorizationMember authorizationMember) {
		// given
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

		// When
		authorizationService.logout(authorizationMember, httpServletRequest, httpServletResponse);
		Cookie cookie = httpServletResponse.getCookie("access_token");

		// Then
		assertThat(httpServletResponse.getCookies()).isEmpty();
	}
}
