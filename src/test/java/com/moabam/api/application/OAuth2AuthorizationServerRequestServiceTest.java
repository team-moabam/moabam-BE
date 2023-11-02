package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthorizationServerRequestServiceTest {

	@InjectMocks
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Mock
	RestTemplate restTemplate;

	String uri = "https://authorization/url?"
		+ "response_type=code&"
		+ "client_id=testtestetsttest&"
		+ "redirect_uri=http://redirect/url&scope=profile_nickname,profile_image";

	@BeforeEach
	void initField() {
		ReflectionTestUtils.setField(oAuth2AuthorizationServerRequestService, "restTemplate", restTemplate);
	}

	@DisplayName("로그인 페이지 접근 요청")
	@Nested
	class LoginPage {

		@DisplayName("로그인 페이지 접근 요청 성공")
		@Test
		void authorization_code_uri_generate_success() throws IOException {
			// given
			MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

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

	@DisplayName("Authorization Server에 토큰 발급 요청")
	@Nested
	class TokenRequest {

		@DisplayName("토큰 발급 요청 성공")
		@Test
		void toekn_issue_request_success() {
			// given
			String tokenUri = "test";
			MultiValueMap<String, String> uriParams = new LinkedMultiValueMap<>();
			ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse = mock(ResponseEntity.class);

			// when
			when(restTemplate.exchange(
				eq(tokenUri),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(AuthorizationTokenResponse.class))
			).thenReturn(authorizationTokenResponse);

			// When
			when(authorizationTokenResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

			// Then
			assertThatNoException().isThrownBy(
				() -> oAuth2AuthorizationServerRequestService.requestAuthorizationServer(tokenUri, uriParams));
		}

		@DisplayName("토큰 발급 요청 실패")
		@ParameterizedTest
		@ValueSource(ints = {400, 401, 403, 429, 500, 502, 503})
		void token_issue_request_fail(int code) {
			// Given
			String tokenUri = "test";
			MultiValueMap<String, String> uriParams = new LinkedMultiValueMap<>();

			ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse = mock(ResponseEntity.class);

			when(restTemplate.exchange(
				eq(tokenUri),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(AuthorizationTokenResponse.class))
			).thenReturn(authorizationTokenResponse);

			// When
			when(authorizationTokenResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(code));

			// Then
			assertThatThrownBy(
				() -> oAuth2AuthorizationServerRequestService.requestAuthorizationServer(tokenUri, uriParams))
				.isInstanceOf(BadRequestException.class)
				.hasMessage(ErrorMessage.REQUEST_FAILED.getMessage());
		}
	}

	@DisplayName("토큰 정보 조회 요청")
	@Nested
	class TokenInfo {

		@DisplayName("토큰 정보 조회 성공")
		@Test
		void toekn_issue_request_success() {
			// given
			String tokenInfoUri = "http://token-info.com/test";
			String tokenValue = "testetstetset";

			ResponseEntity<AuthorizationTokenInfoResponse> infoResponse = mock(ResponseEntity.class);

			// when
			when(restTemplate.exchange(
				eq(tokenInfoUri),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(AuthorizationTokenInfoResponse.class)
			)).thenReturn(infoResponse);

			when(infoResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

			// Then
			assertThatNoException().isThrownBy(
				() -> oAuth2AuthorizationServerRequestService.tokenInfoRequest(tokenInfoUri, tokenValue));
		}

		@DisplayName("토큰 정보 조회 실패")
		@ParameterizedTest
		@ValueSource(ints = {400, 401})
		void request_token_info_success(int code) {
			// given
			String tokenInfoUri = "http://token-info.com/test";
			String tokenValue = "testetstetset";

			ResponseEntity<AuthorizationTokenInfoResponse> infoResponse = mock(ResponseEntity.class);

			// when
			when(restTemplate.exchange(
				eq(tokenInfoUri),
				eq(HttpMethod.GET),
				any(HttpEntity.class),
				eq(AuthorizationTokenInfoResponse.class)
			)).thenReturn(infoResponse);

			when(infoResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(code));

			// then
			assertThatThrownBy(() -> oAuth2AuthorizationServerRequestService.tokenInfoRequest(tokenInfoUri, tokenValue))
				.isInstanceOf(BadRequestException.class)
				.hasMessage(ErrorMessage.REQUEST_FAILED.getMessage());
		}

	}
}
