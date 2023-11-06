package com.moabam.api.presentation;

import static com.moabam.global.common.util.OAuthParameterNames.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.AuthenticationService;
import com.moabam.api.application.OAuth2AuthorizationServerRequestService;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.fixture.AuthorizationCodeResponseFixture;
import com.moabam.fixture.AuthorizationTokenInfoResponseFixture;
import com.moabam.fixture.AuthorizationTokenResponseFixture;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.handler.RestTemplateResponseHandler;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@SpyBean
	AuthenticationService authenticationService;

	@Autowired
	OAuthConfig oAuthConfig;

	static RestTemplateBuilder restTemplateBuilder;

	MockRestServiceServer mockRestServiceServer;

	@BeforeAll
	static void allSetUp() {
		restTemplateBuilder = new RestTemplateBuilder()
			.errorHandler(new RestTemplateResponseHandler());
	}

	@BeforeEach
	void setUp() {
		// TODO 추후 RestTemplate -> REstTemplateBuilder & Bean등록하여 테스트 코드도 일부 변경됨
		RestTemplate restTemplate = restTemplateBuilder.build();
		ReflectionTestUtils.setField(oAuth2AuthorizationServerRequestService, "restTemplate", restTemplate);
		mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
	}

	@DisplayName("인가 코드 받기 위한 로그인 페이지 요청")
	@Test
	void authorization_code_request_success() throws Exception {
		// given
		String uri = UriComponentsBuilder
			.fromUriString(oAuthConfig.provider().authorizationUri())
			.queryParam(RESPONSE_TYPE, "code")
			.queryParam(CLIENT_ID, oAuthConfig.client().clientId())
			.queryParam(REDIRECT_URI, oAuthConfig.provider().redirectUri())
			.queryParam(SCOPE, String.join(",", oAuthConfig.client().scope()))
			.toUriString();

		// expected
		ResultActions result = mockMvc.perform(get("/members"));

		result.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Content-type",
				MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8))
			.andExpect(MockMvcResultMatchers.redirectedUrl(uri));
	}

	@DisplayName("소셜 로그인 및 회원가입 요청 성공")
	@Test
	void social_login_signUp_request_success() throws Exception {
		// given
		MultiValueMap<String, String> contentParams = new LinkedMultiValueMap<>();
		contentParams.add(GRANT_TYPE, oAuthConfig.client().authorizationGrantType());
		contentParams.add(CLIENT_ID, oAuthConfig.client().clientId());
		contentParams.add(REDIRECT_URI, oAuthConfig.provider().redirectUri());
		contentParams.add(CODE, "test");
		contentParams.add(CLIENT_SECRET, oAuthConfig.client().clientSecret());

		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationCodeResponseFixture.successCodeResponse();
		AuthorizationTokenResponse authorizationTokenResponse =
			AuthorizationTokenResponseFixture.authorizationTokenResponse();
		String response = objectMapper.writeValueAsString(authorizationTokenResponse);

		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationTokenInfoResponseFixture.authorizationTokenInfoResponse();
		String tokenInfoResponse = objectMapper.writeValueAsString(authorizationTokenInfoResponse);

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenUri()))
			.andExpect(MockRestRequestMatchers.content().formData(contentParams))
			.andExpect(MockRestRequestMatchers.content().contentType("application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenInfo()))
			.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
			.andExpect(MockRestRequestMatchers.header("Authorization", "Bearer accessToken"))
			.andRespond(withSuccess(tokenInfoResponse, MediaType.APPLICATION_JSON));

		mockMvc.perform(get("/members/login/kakao/oauth")
				.flashAttr("authorizationCodeResponse", authorizationCodeResponse))
			.andExpectAll(
				status().isOk(),
				MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
				header().string("token_type", "Bearer"),
				cookie().exists("access_token"),
				cookie().httpOnly("access_token", true),
				cookie().secure("access_token", true),
				cookie().exists("refresh_token"),
				cookie().httpOnly("refresh_token", true),
				cookie().secure("refresh_token", true)
			)
			.andExpect(jsonPath("$.isSignUp").value(true));
	}

	@DisplayName("Authorization Token 발급 실패")
	@ParameterizedTest
	@ValueSource(ints = {400, 401, 403, 429, 500, 502, 503})
	void authorization_token_request_fail(int code) throws Exception {
		// given
		MultiValueMap<String, String> contentParams = new LinkedMultiValueMap<>();
		contentParams.add(GRANT_TYPE, oAuthConfig.client().authorizationGrantType());
		contentParams.add(CLIENT_ID, oAuthConfig.client().clientId());
		contentParams.add(REDIRECT_URI, oAuthConfig.provider().redirectUri());
		contentParams.add(CODE, "test");
		contentParams.add(CLIENT_SECRET, oAuthConfig.client().clientSecret());

		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationCodeResponseFixture.successCodeResponse();

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenUri()))
			.andExpect(MockRestRequestMatchers.content().formData(contentParams))
			.andExpect(MockRestRequestMatchers.content().contentType("application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatusCode.valueOf(code)));

		mockMvc.perform(get("/members/login/kakao/oauth")
				.flashAttr("authorizationCodeResponse", authorizationCodeResponse))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("토큰 정보 요청 실패")
	@ParameterizedTest
	@ValueSource(ints = {400, 401})
	void token_info_response_fail(int code) throws Exception {
		// given
		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationCodeResponseFixture.successCodeResponse();

		// when
		doReturn(AuthorizationTokenResponseFixture.authorizationTokenResponse())
			.when(authenticationService).requestToken(authorizationCodeResponse);

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenInfo()))
			.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
			.andExpect(MockRestRequestMatchers.header("Authorization", "Bearer accessToken"))
			.andRespond(withStatus(HttpStatusCode.valueOf(code)));

		mockMvc.perform(get("/members/login/kakao/oauth")
				.flashAttr("authorizationCodeResponse", authorizationCodeResponse))
			.andExpect(status().isBadRequest());
	}
}
