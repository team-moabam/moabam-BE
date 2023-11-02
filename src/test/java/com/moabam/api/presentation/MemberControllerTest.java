package com.moabam.api.presentation;

import static com.moabam.global.common.util.OAuthParameterNames.CLIENT_ID;
import static com.moabam.global.common.util.OAuthParameterNames.CLIENT_SECRET;
import static com.moabam.global.common.util.OAuthParameterNames.CODE;
import static com.moabam.global.common.util.OAuthParameterNames.GRANT_TYPE;
import static com.moabam.global.common.util.OAuthParameterNames.REDIRECT_URI;
import static com.moabam.global.common.util.OAuthParameterNames.RESPONSE_TYPE;
import static com.moabam.global.common.util.OAuthParameterNames.SCOPE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
import com.moabam.api.application.OAuth2AuthorizationServerRequestService;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.fixture.AuthorizationTokenResponseFixture;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Autowired
	OAuthConfig oAuthConfig;

	static RestTemplate restTemplate;

	MockRestServiceServer mockRestServiceServer;

	@BeforeAll
	static void allSetUp() {
		restTemplate = new RestTemplate();
	}

	@BeforeEach
	void setUp() {
		// TODO 추후 RestTemplate -> REstTemplateBuilder & Bean등록하여 테스트 코드도 일부 변경됨
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

	@DisplayName("Authorization Server에 토큰 발급 요청")
	@Test
	void authorization_token_request_success() throws Exception {
		// given
		MultiValueMap<String, String> contentParams = new LinkedMultiValueMap<>();
		contentParams.add(GRANT_TYPE, oAuthConfig.client().authorizationGrantType());
		contentParams.add(CLIENT_ID, oAuthConfig.client().clientId());
		contentParams.add(REDIRECT_URI, oAuthConfig.provider().redirectUri());
		contentParams.add(CODE, "test");
		contentParams.add(CLIENT_SECRET, oAuthConfig.client().clientSecret());

		String response = objectMapper.writeValueAsString(
			AuthorizationTokenResponseFixture.authorizationTokenResponse());
		AuthorizationCodeResponse authorizationCodeResponse = new AuthorizationCodeResponse("test", null, null, null);

		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().tokenUri()))
			.andExpect(MockRestRequestMatchers.content().formData(contentParams))
			.andExpect(MockRestRequestMatchers.content().contentType("application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		// expected
		ResultActions result = mockMvc.perform(get("/members/login/kakao/oauth")
				.flashAttr("authorizationCodeResponse", authorizationCodeResponse))
			.andExpect(status().isOk());
	}
}
