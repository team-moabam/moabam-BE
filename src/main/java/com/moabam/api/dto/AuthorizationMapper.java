package com.moabam.api.dto;

import org.json.JSONObject;

import com.moabam.api.domain.entity.enums.Role;
import com.moabam.global.config.OAuthConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationMapper {

	public static AuthorizationCodeRequest toAuthorizationCodeRequest(OAuthConfig oAuthConfig) {
		return AuthorizationCodeRequest.builder()
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.scope(oAuthConfig.client().scope())
			.build();
	}

	public static AuthorizationTokenRequest toAuthorizationTokenRequest(OAuthConfig oAuthConfig, String code) {
		return AuthorizationTokenRequest.builder()
			.grantType(oAuthConfig.client().authorizationGrantType())
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.code(code)
			.clientSecret(oAuthConfig.client().clientSecret())
			.build();
	}

	public static PublicClaim toPublicClaim(JSONObject jsonObject) {
		return PublicClaim.builder()
			.id(Long.valueOf(jsonObject.get("id").toString()))
			.nickname(jsonObject.getString("nickname"))
			.role(jsonObject.getEnum(Role.class, "role"))
			.build();
	}

	public static AuthorizationMember toAuthorizationMember(PublicClaim publicClaim) {
		return new AuthorizationMember(publicClaim.id(), publicClaim.nickname(), publicClaim.role());
	}
}
