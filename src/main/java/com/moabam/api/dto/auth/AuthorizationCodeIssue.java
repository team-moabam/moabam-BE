package com.moabam.api.dto.auth;

import com.moabam.global.common.util.GlobalConstant;
import lombok.Builder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.moabam.global.common.util.OAuthParameterNames.*;

public record AuthorizationCodeIssue(
	String clientId,
	String redirectUri,
	String responseType,
	List<String> scope,
	String state
) {

	@Builder
	public AuthorizationCodeIssue(String clientId, String redirectUri, String responseType, List<String> scope,
								  String state) {
		this.clientId = clientId;
		this.redirectUri = redirectUri;
		this.responseType = responseType;
		this.scope = scope;
		this.state = state;
	}

	public String generateQueryParamsWith(String baseUrl) {
		UriComponentsBuilder authorizationCodeUri = UriComponentsBuilder
			.fromPath(baseUrl)
			.queryParam(RESPONSE_TYPE, CODE)
			.queryParam(CLIENT_ID, clientId)
			.queryParam(REDIRECT_URI, redirectUri);

		if (!scope.isEmpty()) {
			String scopes = String.join(GlobalConstant.COMMA, scope);
			authorizationCodeUri.queryParam(SCOPE, scopes);
		}

		return authorizationCodeUri.toUriString();
	}
}
