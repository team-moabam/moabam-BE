package com.moabam.api.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
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

	@BeforeEach
	void initField() {
		ReflectionTestUtils.setField(oAuth2AuthorizationServerRequestService, "restTemplate", restTemplate);
	}

	@DisplayName("로그인 페이지 접근 요청")
	@Nested
	class LoginPage {

		String uri = "https://authorization/url?"
			+ "response_type=code&"
			+ "client_id=testtestetsttest&"
			+ "redirect_uri=http://redirect/url&scope=profile_nickname,profile_image";

		@DisplayName("로그인 페이지 접근 요청 성공")
		@Test
		void authorization_code_uri_generate_success() {
			// Given
			MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

			// When
			oAuth2AuthorizationServerRequestService.loginRequest(mockHttpServletResponse, uri);

			// Then
			assertThat(mockHttpServletResponse.getContentType())
				.isEqualTo(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
			assertThat(mockHttpServletResponse.getRedirectedUrl()).isEqualTo(uri);
		}

		@DisplayName("redirect 실패 테스트")
		@Test
		void redirect_fail() {
			// Given
			HttpServletResponse mockHttpServletResponse = mock(HttpServletResponse.class);

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

	@DisplayName("Authorization Server 토큰 발급 요청")
	@Nested
	class TokenRequest {

		@DisplayName("토큰 발급 요청 성공")
		@Test
		void token_issue_request_success() {
			// Given
			String tokenUri = "test";
			MultiValueMap<String, String> uriParams = new LinkedMultiValueMap<>();

			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(uriParams, headers);

			// When
			doReturn(new ResponseEntity<AuthorizationTokenResponse>(HttpStatus.OK))
				.when(restTemplate).exchange(
					eq(tokenUri),
					eq(HttpMethod.POST),
					any(HttpEntity.class),
					eq(AuthorizationTokenResponse.class));
			oAuth2AuthorizationServerRequestService.requestAuthorizationServer(tokenUri, uriParams);

			// Then
			verify(restTemplate, times(1))
				.exchange(tokenUri, HttpMethod.POST, httpEntity, AuthorizationTokenResponse.class);
		}

		@DisplayName("토큰 발급 요청 실패")
		@ParameterizedTest
		@ValueSource(ints = {400, 401, 403, 429, 500, 502, 503})
		void token_issue_request_fail(int code) {
			// Given
			String tokenUri = "test";
			MultiValueMap<String, String> uriParams = new LinkedMultiValueMap<>();

			// When
			doThrow(new HttpClientErrorException(HttpStatusCode.valueOf(code)))
				.when(restTemplate).exchange(
					eq(tokenUri),
					eq(HttpMethod.POST),
					any(HttpEntity.class),
					eq(AuthorizationTokenResponse.class));

			// Then
			assertThatThrownBy(() ->
				oAuth2AuthorizationServerRequestService.requestAuthorizationServer(tokenUri, uriParams))
				.isInstanceOf(HttpClientErrorException.class);
		}
	}

	@DisplayName("토큰 정보 조회 발급 요청")
	@Nested
	class TokenInfoRequest {

		@DisplayName("토큰 정보 조회 요청 성공")
		@Test
		void token_info_request_success() {
			// Given
			String tokenInfoUri = "http://tokenInfo/uri";
			String tokenValue = "Bearer access-token";

			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", tokenValue);
			HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

			// When
			doReturn(new ResponseEntity<AuthorizationTokenInfoResponse>(HttpStatus.OK))
				.when(restTemplate).exchange(
					eq(tokenInfoUri),
					eq(HttpMethod.GET),
					any(HttpEntity.class),
					eq(AuthorizationTokenInfoResponse.class));
			oAuth2AuthorizationServerRequestService.tokenInfoRequest(tokenInfoUri, tokenValue);

			// Then
			verify(restTemplate, times(1))
				.exchange(tokenInfoUri, HttpMethod.GET, httpEntity, AuthorizationTokenInfoResponse.class);
		}

		@DisplayName("토큰 발급 요청 실패")
		@ParameterizedTest
		@ValueSource(ints = {400, 401})
		void token_issue_request_fail(int code) {
			// Given
			String tokenInfoUri = "http://tokenInfo/uri";
			String tokenValue = "Bearer access-token";

			// When
			doThrow(new HttpClientErrorException(HttpStatusCode.valueOf(code)))
				.when(restTemplate).exchange(
					eq(tokenInfoUri),
					eq(HttpMethod.GET),
					any(HttpEntity.class),
					eq(AuthorizationTokenInfoResponse.class));

			// Then
			assertThatThrownBy(() ->
				oAuth2AuthorizationServerRequestService.tokenInfoRequest(tokenInfoUri, tokenValue))
				.isInstanceOf(HttpClientErrorException.class);
		}
	}

	@DisplayName("회원 연결 끊기 요청")
	@Nested
	class Delete {

		@DisplayName("성공")
		@Test
		void token_info_request_success() {
			// Given
			String deleteUri = "https://deleteUrl/uri";
			String adminKey = "admin-token";
			String socialId = "1";

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8);
			headers.add("Authorization", "KakaoAK " + adminKey);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("target_id_type", "user_id");
			params.add("target_id", socialId);

			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

			// When
			doReturn(new ResponseEntity<Void>(HttpStatus.OK))
				.when(restTemplate).exchange(
					eq(deleteUri),
					eq(HttpMethod.POST),
					any(HttpEntity.class),
					eq(Void.class));
			oAuth2AuthorizationServerRequestService.unlinkMemberRequest(deleteUri, adminKey, params);

			// Then
			verify(restTemplate, times(1))
				.exchange(deleteUri, HttpMethod.POST, httpEntity, Void.class);
		}

		@DisplayName("실패")
		@ParameterizedTest
		@ValueSource(ints = {400, 401})
		void token_issue_request_fail(int code) {
			// Given
			String deleteUri = "https://deleteUrl/uri";
			String adminKey = "admin-token";
			String socialId = "1";

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("target_id_type", "user_id");
			params.add("target_id", socialId);

			// When
			doThrow(new HttpClientErrorException(HttpStatusCode.valueOf(code)))
				.when(restTemplate).exchange(
					eq(deleteUri),
					eq(HttpMethod.POST),
					any(HttpEntity.class),
					eq(Void.class));

			// Then
			assertThatThrownBy(() ->
				oAuth2AuthorizationServerRequestService.unlinkMemberRequest(deleteUri, adminKey, params))
				.isInstanceOf(HttpClientErrorException.class);
		}
	}
}
