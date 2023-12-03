package com.moabam.support.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.moabam.api.application.auth.JwtProviderService;
import com.moabam.api.domain.member.Role;
import com.moabam.global.common.util.cookie.CookieUtils;
import com.moabam.global.config.TokenConfig;
import com.moabam.support.fixture.PublicClaimFixture;

@SpringBootTest
public class WithFilterSupporter {

	@RegisterExtension
	RestDocumentationExtension restDocumentationExtension = new RestDocumentationExtension();

	@Autowired
	WebApplicationContext webApplicationContext;

	@Autowired
	JwtProviderService jwtProviderService;

	@Autowired
	TokenConfig tokenConfig;

	@Autowired
	CookieUtils cookieUtils;

	protected MockMvc mockMvc;

	@BeforeEach
	void setUpMockMvc(RestDocumentationContextProvider contextProvider) {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(RestDocsFactory.restdocs(contextProvider))
			.defaultRequest(get("/")
				.cookie(cookieUtils.typeCookie("Bearer", tokenConfig.getRefreshExpire()))
				.cookie(cookieUtils.tokenCookie("access_token",
					jwtProviderService.provideAccessToken(PublicClaimFixture.publicClaim()),
					tokenConfig.getRefreshExpire()))
				.cookie(cookieUtils.tokenCookie("refresh_token",
					jwtProviderService.provideRefreshToken(Role.USER),
					tokenConfig.getRefreshExpire())))
			.build();
	}
}
