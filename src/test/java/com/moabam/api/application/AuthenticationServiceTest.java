package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.moabam.global.config.OAuthConfig;

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

	@DisplayName("authentication code Url 생성 성공")
	@Test
	void authenticationUrl() {
		// Given + When
		String authorizaionCodeUri = authenticationService.getAuthorizaionCodeUri();

		// Then
		assertThat(authorizaionCodeUri).contains(oauthConfig.client().clientId(),
			String.join(",", oauthConfig.client().scope()), oauthConfig.provider().redirectUri());
	}

	@DisplayName("Client id가 없을 때 url 생성 실패")
	@Test
	void create_failby_no_client_id() {
		// Given
		AuthenticationService noPropertyService = new AuthenticationService(new OAuthConfig(
			new OAuthConfig.Provider(null, null),
			new OAuthConfig.Client(null, null, null, null)
		));

		// When + Then
		assertThatThrownBy(noPropertyService::getAuthorizaionCodeUri)
			.isInstanceOf(NullPointerException.class);
	}
}
