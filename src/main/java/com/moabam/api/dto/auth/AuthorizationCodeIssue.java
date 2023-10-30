package com.moabam.api.dto.auth;

import com.moabam.global.common.util.GlobalConstant;
import lombok.Builder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

	public MultiValueMap<String, String> generateQueryParams() {
		MultiValueMap<String, String> oauthParam = new LinkedMultiValueMap<>();
		oauthParam.add(RESPONSE_TYPE, CODE);
		oauthParam.add(CLIENT_ID, clientId);
		oauthParam.add(REDIRECT_URL, redirectUri);

		if (!scope.isEmpty()) {
			String scopes = String.join(GlobalConstant.COMMA, scope);
			oauthParam.add(SCOPE, scopes);
		}

		return oauthParam;
	}
}
