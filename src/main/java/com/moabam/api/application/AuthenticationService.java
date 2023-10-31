package com.moabam.api.application;

import static com.moabam.global.common.util.OAuthParameterNames.*;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.OAuthMapper;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final OAuthConfig oAuthConfig;

	private String getAuthorizaionCodeUri() {
		AuthorizationCodeRequest authorizationCodeRequest = OAuthMapper.toAuthorizationCodeRequest(oAuthConfig);
		return generateQueryParamsWith(authorizationCodeRequest);
	}

	private String generateQueryParamsWith(AuthorizationCodeRequest authorizationCodeRequest) {
		UriComponentsBuilder authorizationCodeUri = UriComponentsBuilder
			.fromUriString(oAuthConfig.provider().authorizationUri())
			.queryParam(RESPONSE_TYPE, CODE)
			.queryParam(CLIENT_ID, authorizationCodeRequest.clientId())
			.queryParam(REDIRECT_URI, authorizationCodeRequest.redirectUri());

		if (!authorizationCodeRequest.scope().isEmpty()) {
			String scopes = String.join(GlobalConstant.COMMA, authorizationCodeRequest.scope());
			authorizationCodeUri.queryParam(SCOPE, scopes);
		}

		return authorizationCodeUri.toUriString();
	}

	public void redirectToLoginPage(HttpServletResponse httpServletResponse) {
		String authorizationCodeUri = getAuthorizaionCodeUri();

		try {
			httpServletResponse.setContentType(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
			httpServletResponse.sendRedirect(authorizationCodeUri);
		} catch (IOException e) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILD);
		}
	}
}
