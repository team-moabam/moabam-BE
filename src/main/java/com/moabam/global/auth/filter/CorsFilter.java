package com.moabam.global.auth.filter;

import java.io.IOException;
import java.util.Objects;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.google.cloud.storage.HttpMethod;
import com.moabam.global.config.AllowOriginConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class CorsFilter extends OncePerRequestFilter {

	private static final String ALLOWED_METHOD_NAMES = "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH";
	private static final String ALLOWED_HEADERS = "Origin, Accept, Access-Control-Request-Method, "
		+ "Access-Control-Request-Headers, X-Requested-With,Content-Type, Referer";

	private final HandlerExceptionResolver handlerExceptionResolver;

	private final AllowOriginConfig allowOriginsConfig;

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
		FilterChain filterChain) throws ServletException, IOException {
		String refer = httpServletRequest.getHeader("referer");
		String origin = secureMatch(refer);

		try {
			if (Objects.isNull(origin)) {
				throw new UnauthorizedException(ErrorMessage.INVALID_REQUEST_URL);
			}
		} catch (UnauthorizedException unauthorizedException) {
			log.error("{}, {}", httpServletRequest.getHeader("referer"), allowOriginsConfig.origin());
			handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null,
				unauthorizedException);

			return;
		}

		httpServletResponse.setHeader("Access-Control-Allow-Origin", origin);
		httpServletResponse.setHeader("Access-Control-Allow-Methods", ALLOWED_METHOD_NAMES);
		httpServletResponse.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
		httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpServletResponse.setHeader("Access-Control-Max-Age", "3600");

		if (isOption(httpServletRequest.getMethod())) {
			httpServletRequest.setAttribute("isPermit", true);
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	public String secureMatch(String refer) {
		return allowOriginsConfig.origin().stream().filter(refer::contains).findFirst().orElse(null);
	}

	public boolean isOption(String method) {
		return HttpMethod.OPTIONS.name().equals(method);
	}
}
