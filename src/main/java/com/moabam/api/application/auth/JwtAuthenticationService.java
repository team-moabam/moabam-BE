package com.moabam.api.application.auth;

import java.util.Base64;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

	private final TokenConfig tokenConfig;

	public boolean isTokenExpire(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(tokenConfig.getKey())
				.build()
				.parseClaimsJws(token);
			return false;
		} catch (ExpiredJwtException expiredJwtException) {
			return true;
		} catch (Exception exception) {
			throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
		}
	}

	public PublicClaim parseClaim(String token) {
		String claims = token.split("\\.")[1];
		String decodeClaims = new String(Base64.getDecoder().decode(claims));
		JSONObject jsonObject = new JSONObject(decodeClaims);

		return AuthorizationMapper.toPublicClaim(jsonObject);
	}
}
