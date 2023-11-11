package com.moabam.global.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.moabam.global.common.handler.PathResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Order(1)
@Component
@RequiredArgsConstructor
public class PathFilter extends OncePerRequestFilter {

	private final PathResolver pathResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		Optional<PathResolver.Path> matchedPath = pathResolver.permitPathMatch(request.getRequestURI());

		matchedPath.ifPresent(path -> {
			if (path.httpMethods().stream()
				.anyMatch(httpMethod -> httpMethod.matches(request.getMethod()))) {
				request.setAttribute("isPermit", true);
			}
		});

		filterChain.doFilter(request, response);
	}
}
