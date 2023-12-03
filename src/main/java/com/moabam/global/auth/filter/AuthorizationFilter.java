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
import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.error.exception.BadRequestException;
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
		@NotNull HttpServletResponse httpServletResponse, @NotNull FilterChain filterChain) throws
		ServletException,
		IOException {

		if (isPermit(httpServletRequest)) {
			filterChain.doFilter(httpServletRequest, httpServletResponse);
			return;
		}

		try {
			invoke(httpServletRequest, httpServletResponse);
		} catch (UnauthorizedException unauthorizedException) {
			authorizationService.removeToken(httpServletRequest, httpServletResponse);
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

		handleTokenAuthenticate(cookies, httpServletResponse, httpServletRequest);
	}

	private boolean isTokenTypeBearer(Cookie[] cookies) {
		return "Bearer".equals(extractTokenFromCookie(cookies, "token_type"));
	}

	private void handleTokenAuthenticate(Cookie[] cookies, HttpServletResponse httpServletResponse,
		HttpServletRequest httpServletRequest) {
		String accessToken = extractTokenFromCookie(cookies, "access_token");
		PublicClaim publicClaim = authenticationService.parseClaim(accessToken);

		if (authenticationService.isTokenExpire(accessToken, publicClaim.role())) {
			String refreshToken = extractTokenFromCookie(cookies, "refresh_token");

			if (authenticationService.isTokenExpire(refreshToken, publicClaim.role())) {
				throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
			}

			validInvalidMember(publicClaim, refreshToken, httpServletRequest);
			authorizationService.issueServiceToken(httpServletResponse, publicClaim);
		}

		AuthorizationThreadLocal.setAuthMember(AuthorizationMapper.toAuthMember(publicClaim));
	}

	private void validInvalidMember(PublicClaim publicClaim, String refreshToken,
		HttpServletRequest httpServletRequest) {
		boolean isAdminPath = httpServletRequest.getRequestURI().contains("admins");

		if (!((publicClaim.role().equals(Role.ADMIN) && isAdminPath) || (publicClaim.role().equals(Role.USER)
			&& !isAdminPath))) {
			throw new BadRequestException(ErrorMessage.INVALID_REQUEST_ROLE);
		}

		authorizationService.validTokenPair(publicClaim.id(), refreshToken, publicClaim.role());
		authorizationService.validMemberExist(publicClaim.id(), publicClaim.role());
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
}
