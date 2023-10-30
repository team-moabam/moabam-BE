package com.moabam.api.application.auth;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.global.config.OAuthConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

	@InjectMocks
	AuthenticationService authenticationService;

	OAuthConfig oauthConfig;

	@BeforeEach
	public void initParams() {
		oauthConfig = new OAuthConfig(
			new OAuthConfig.Provider("https://test.com/test/test", "http://test:8080/test"),
			new OAuthConfig.Client("provider", "testtestetsttest", "authorization_code",
				List.of("profile_nickname", "profile_image"))
		);
		ReflectionTestUtils.setField(authenticationService, "oAuthConfig", oauthConfig);
	}

	@DisplayName("")
	@Test
	void authenticationUrl() {
		// Given + When
		AuthorizationCodeRequest codeRequest = authenticationService.authorizaionCodeParams();

		// Then
		assertAll(
			() -> assertThat(codeRequest.clientId()).isEqualTo(oauthConfig.client().clientId()),
			() -> assertThat(codeRequest.scope()).isEqualTo(oauthConfig.client().scope()),
			() -> assertThat(codeRequest.redirectUri()).isEqualTo(oauthConfig.provider().redirectUrl())
			);
	}
}
