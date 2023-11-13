package com.moabam.api.dto;

import static java.util.Objects.*;

import java.util.List;

import org.springframework.http.HttpMethod;

import com.moabam.api.domain.entity.enums.Role;
import com.moabam.global.common.handler.PathResolver;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathMapper {

	public static PathResolver.Path parsePath(String uri) {
		return parsePath(uri, null, null);
	}

	public static <T> PathResolver.Path parsePath(String uri, @Nonnull List<T> params) {
		if (!params.isEmpty() && params.get(0) instanceof Role) {
			return parsePath(uri, (List<Role>)params, null);
		}
		return parsePath(uri, null, (List<HttpMethod>)params);
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
