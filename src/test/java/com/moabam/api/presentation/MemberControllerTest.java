package com.moabam.api.presentation;

import static com.moabam.global.common.util.OAuthParameterNames.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.AuthenticationService;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;

@SpringBootTest(
	properties = {
		"oauth2.provider.authorization-uri=https://kauth.kakao.com/oauth/authorize",
		"oauth2.provider.redirect-uri=http://localhost:8080/members/login/kakao/oauth",
		"oauth2.client.client-id=testtesttesttesttesttesttesttesttest",
		"oauth2.client.authorization-grant-type=authorization_code",
		"oauth2.client.scope=profile_nickname,profile_image"
	}
)
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	OAuthConfig oAuthConfig;

	@DisplayName("인가 코드 받기 위한 로그인 페이지 요청")
	@Test
	void authorization_code_request_success() throws Exception {
		// given
		String uri = UriComponentsBuilder
			.fromPath(oAuthConfig.provider().authorizationUri())
			.queryParam(RESPONSE_TYPE, "code")
			.queryParam(CLIENT_ID, oAuthConfig.client().clientId())
			.queryParam(REDIRECT_URI, oAuthConfig.provider().redirectUri())
			.queryParam(SCOPE, String.join(",", oAuthConfig.client().scope()))
			.toUriString();

		// when
		ResultActions result = mockMvc.perform(get("/members"));

		// then
		result.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Content-type",
				MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8))
			.andExpect(redirectedUrl(uri));
	}
}
