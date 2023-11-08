package com.moabam.api.application;

import java.util.Base64;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

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

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(tokenConfig.getKey())
				.build()
				.parseClaimsJwt(token);
			return true;
		} catch (ExpiredJwtException expiredJwtException) {
			return false;
		} catch (Exception exception) {
			throw new UnauthorizedException(ErrorMessage.AUTHENTICATIE_FAIL);
		}
	}

	public String parseEmail(String token) {
		String claims = token.split("\\.")[1];
		String decodeClaims = new String(Base64.getDecoder().decode(claims));
		JSONObject jsonObject = new JSONObject(decodeClaims);

		return (String)jsonObject.get("id");
	}
}
