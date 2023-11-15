package com.moabam.global.auth.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.auth.JwtAuthenticationService;
import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver handlerExceptionResolver;
	private final JwtAuthenticationService authenticationService;
	private final AuthorizationService authorizationService;

	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest,
		@NotNull HttpServletResponse httpServletResponse,
		@NotNull FilterChain filterChain) throws ServletException, IOException {

		if (isPermit(httpServletRequest)) {
			filterChain.doFilter(httpServletRequest, httpServletResponse);
			return;
		}

		try {
			invoke(httpServletRequest, httpServletResponse);
		} catch (UnauthorizedException unauthorizedException) {
			log.error("Login Failed");
			expireToken(httpServletRequest, httpServletResponse);
			handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null,
				unauthorizedException);

			return;
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	private boolean isPermit(HttpServletRequest httpServletRequest) {
		Boolean isPermit = (Boolean)httpServletRequest.getAttribute("isPermit");

		return Objects.nonNull(isPermit) && Boolean.TRUE.equals(isPermit);
	}

	private void invoke(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Cookie[] cookies = getCookiesOrThrow(httpServletRequest);

		if (!isTokenTypeBearer(cookies)) {
			throw new UnauthorizedException(ErrorMessage.GRANT_FAILED);
		}

		handleTokenAuthenticate(cookies, httpServletResponse);
	}

	private boolean isTokenTypeBearer(Cookie[] cookies) {
		return "Bearer".equals(extractTokenFromCookie(cookies, "token_type"));
	}

	private void handleTokenAuthenticate(Cookie[] cookies,
		HttpServletResponse httpServletResponse) {
		String accessToken = extractTokenFromCookie(cookies, "access_token");
		PublicClaim publicClaim = authenticationService.parseClaim(accessToken);

		if (authenticationService.isTokenExpire(accessToken)) {
			String refreshToken = extractTokenFromCookie(cookies, "refresh_token");

			if (authenticationService.isTokenExpire(refreshToken)) {
				throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
			}

			// 토큰이 내가 발급해 준 토큰인지 확인
			authorizationService.validTokenPair(publicClaim.id(), refreshToken);
			authorizationService.issueServiceToken(httpServletResponse, publicClaim);
		}

		AuthorizationThreadLocal.setAuthorizationMember(AuthorizationMapper.toAuthorizationMember(publicClaim));
	}

	private Cookie[] getCookiesOrThrow(HttpServletRequest httpServletRequest) {
		return Optional.ofNullable(httpServletRequest.getCookies())
			.orElseThrow(() -> new UnauthorizedException(ErrorMessage.GRANT_FAILED));
	}

	private String extractTokenFromCookie(Cookie[] cookies, String tokenName) {
		return Arrays.stream(cookies)
			.filter(cookie -> tokenName.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElseThrow(() -> new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL));
	}

	private void expireToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		if (httpServletRequest.getCookies() == null) {
			return;
		}

		Arrays.stream(httpServletRequest.getCookies())
			.forEach(cookie -> {
				if (cookie.getName().contains("token")) {
					cookie.setMaxAge(0);
					httpServletResponse.addCookie(cookie);
				}
			});
	}
}
