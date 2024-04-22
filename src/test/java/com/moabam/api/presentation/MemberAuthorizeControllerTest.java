package com.moabam.api.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.auth.OAuth2AuthorizationServerRequestService;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.global.auth.filter.CorsFilter;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.handler.RestTemplateResponseHandler;
import com.moabam.support.fixture.AuthorizationResponseFixture;
import com.moabam.support.fixture.ItemFixture;

@SpringBootTest
@AutoConfigureMockMvc
class MemberAuthorizeControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Autowired
	ItemRepository itemRepository;

	@SpyBean
	AuthorizationService authorizationService;

	@SpyBean
	CorsFilter corsFilter;

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
		willReturn("http://localhost").given(corsFilter).getReferer(any());
	}

	@DisplayName("인가 코드 받기 위한 로그인 페이지 요청")
	@Test
	void authorization_code_request_success() throws Exception {
		// given
		String uri = UriComponentsBuilder
			.fromUriString(oAuthConfig.provider().authorizationUri())
			.queryParam("response_type", "code")
			.queryParam("client_id", oAuthConfig.client().clientId())
			.queryParam("redirect_uri", oAuthConfig.provider().redirectUri())
			.queryParam("scope", String.join(",", oAuthConfig.client().scope()))
			.toUriString();

		// expected
		ResultActions result = mockMvc.perform(get("/members/login/oauth"));

		result.andExpect(status().is3xxRedirection())
			.andExpect(MockMvcResultMatchers.header().string("Content-type",
				MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8))
			.andExpect(MockMvcResultMatchers.redirectedUrl(uri));
	}

	@DisplayName("소셜 로그인 및 회원가입 요청 성공")
	@Test
	void social_login_signUp_request_success() throws Exception {
		// given
		MultiValueMap<String, String> contentParams = new LinkedMultiValueMap<>();
		contentParams.add("grant_type", oAuthConfig.client().authorizationGrantType());
		contentParams.add("client_id", oAuthConfig.client().clientId());
		contentParams.add("redirect_uri", oAuthConfig.provider().redirectUri());
		contentParams.add("code", "test");
		contentParams.add("client_secret", oAuthConfig.client().clientSecret());

		Item morningEgg = ItemFixture.morningSantaSkin().build();
		Item nightEgg = ItemFixture.nightMageSkin();
		itemRepository.saveAll(List.of(morningEgg, nightEgg));

		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationResponseFixture.successCodeResponse();
		String requestBody = objectMapper.writeValueAsString(authorizationCodeResponse);
		AuthorizationTokenResponse authorizationTokenResponse =
			AuthorizationResponseFixture.authorizationTokenResponse();
		String response = objectMapper.writeValueAsString(authorizationTokenResponse);

		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			AuthorizationResponseFixture.authorizationTokenInfoResponse();
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

		mockMvc.perform(post("/members/login/kakao/oauth")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpectAll(
				status().isOk(),
				MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
				cookie().value("token_type", "Bearer"),
				cookie().exists("access_token"),
				cookie().httpOnly("access_token", true),
				cookie().secure("access_token", true),
				cookie().exists("refresh_token"),
				cookie().httpOnly("refresh_token", true),
				cookie().secure("refresh_token", true)
			)
			.andExpect(MockMvcResultMatchers.jsonPath("$.isSignUp").value(true));
	}

	@DisplayName("Authorization Token 발급 실패")
	@ParameterizedTest
	@ValueSource(ints = {400, 401, 403, 429, 500, 502, 503})
	void authorization_token_request_fail(int code) throws Exception {
		// given
		MultiValueMap<String, String> contentParams = new LinkedMultiValueMap<>();
		contentParams.add("grant_type", oAuthConfig.client().authorizationGrantType());
		contentParams.add("client_id", oAuthConfig.client().clientId());
		contentParams.add("redirect_uri", oAuthConfig.provider().redirectUri());
		contentParams.add("code", "test");
		contentParams.add("client_secret", oAuthConfig.client().clientSecret());

		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationResponseFixture.successCodeResponse();
		String requestBody = objectMapper.writeValueAsString(authorizationCodeResponse);

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenUri()))
			.andExpect(MockRestRequestMatchers.content().formData(contentParams))
			.andExpect(MockRestRequestMatchers.content().contentType("application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatusCode.valueOf(code)));

		mockMvc.perform(post("/members/login/kakao/oauth")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("토큰 정보 요청 실패")
	@ParameterizedTest
	@ValueSource(ints = {400, 401})
	void token_info_response_fail(int code) throws Exception {
		// given
		AuthorizationCodeResponse authorizationCodeResponse = AuthorizationResponseFixture.successCodeResponse();

		// when
		doReturn(AuthorizationResponseFixture.authorizationTokenResponse())
			.when(authorizationService).requestToken(authorizationCodeResponse);

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenInfo()))
			.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
			.andExpect(MockRestRequestMatchers.header("Authorization", "Bearer accessToken"))
			.andRespond(withStatus(HttpStatusCode.valueOf(code)));

		mockMvc.perform(post("/members/login/kakao/oauth")
				.content(objectMapper.writeValueAsString(authorizationCodeResponse))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
}
