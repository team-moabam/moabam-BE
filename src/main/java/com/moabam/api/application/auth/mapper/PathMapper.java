package com.moabam.api.application.auth.mapper;

import static java.util.Objects.*;

import java.util.List;

import org.springframework.http.HttpMethod;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.handler.PathResolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathMapper {

	public static PathResolver.Path parsePath(String uri) {
		return parsePath(uri, null, null);
	}

	public static PathResolver.Path pathWithRole(String uri, List<Role> params) {
		return parsePath(uri, params, null);
	}

	public static PathResolver.Path pathWithMethod(String uri, List<HttpMethod> params) {
		return parsePath(uri, null, params);
	}

	private static PathResolver.Path parsePath(String uri, List<Role> roles, List<HttpMethod> methods) {
		PathResolver.Path.PathBuilder pathBuilder = PathResolver.Path.builder().uri(uri);

		if (nonNull(roles)) {
			pathBuilder.roles(roles);
		}

		if (nonNull(methods)) {
			pathBuilder.httpMethods(methods);
		}

		return pathBuilder.build();
	}
}
