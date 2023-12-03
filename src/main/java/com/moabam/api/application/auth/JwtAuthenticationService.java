package com.moabam.api.application.auth;

import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final TokenConfig tokenConfig;

    public boolean isTokenExpire(String token, Role role) {
        try {
            Key key = getSecret(role);

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return false;
        } catch (ExpiredJwtException expiredJwtException) {
            return true;
        } catch (Exception exception) {
            throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
        }
    }

    private Key getSecret(Role role) {
        if (role.equals(Role.ADMIN)) {
            return tokenConfig.getAdminKey();
        }

        return tokenConfig.getKey();
    }

    public PublicClaim parseClaim(String token) {
        String claims = token.split("\\.")[1];
        byte[] claimsBytes = Decoders.BASE64URL.decode(claims);
        String decodedClaims = new String(claimsBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(decodedClaims);

        return AuthorizationMapper.toPublicClaim(jsonObject);
    }
}
