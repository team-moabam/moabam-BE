package com.moabam.global.common.util;

import static java.util.Objects.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.moabam.api.domain.entity.enums.Role;

import lombok.Builder;
import lombok.Singular;

public class PathResolver {

	private final Map<PathPattern, Path> permitPatterns;
	private final Map<PathPattern, Path> authenticationPatterns;

	public PathResolver(Paths paths) {
		this.permitPatterns = Paths.pathParser(paths.permitAll);
		this.authenticationPatterns = Paths.pathParser(paths.authentications);
	}

	public Optional<Path> permitPathMatch(String uri) {
		return match(permitPatterns, uri);
	}

	public Optional<Path> authenticationsPatterns(String uri) {
		return match(authenticationPatterns, uri);
	}

	private Optional<Path> match(Map<PathPattern, Path> patterns, String uri) {
		Set<PathPattern> paths = patterns.keySet();
		PathContainer path = PathContainer.parsePath(uri);
		PathPattern matchedPattern = paths.stream()
			.filter(pathPattern -> pathPattern.matches(path))
			.findAny()
			.orElse(null);

		return Optional.ofNullable(patterns.get(matchedPattern));
	}

	@Builder
	public record Paths(
		@Singular("permitOne") List<Path> permitAll,
		@Singular("authentication") List<Path> authentications
	) {

		static Map<PathPattern, Path> pathParser(List<Path> uris) {
			PathPatternParser parser = new PathPatternParser();
			return uris.stream()
				.collect(Collectors.toMap(
					path -> parser.parse(path.uri()), Function.identity()
				));
		}
	}

	public record Path(
		String uri,
		List<HttpMethod> httpMethods,
		List<Role> roles
	) {

		private static final List<HttpMethod> BASE_METHODS =
			List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH);

		@Builder
		public Path(String uri, @Singular("httpMethod") List<HttpMethod> httpMethods,
			@Singular("role") List<Role> roles) {
			this.uri = requireNonNull(uri);
			this.roles = Optional.of(roles).filter(role -> !role.isEmpty()).orElse(List.of(Role.USER));
			this.httpMethods = Optional.of(httpMethods)
				.filter(httpMethod -> !httpMethod.isEmpty())
				.orElse(BASE_METHODS);
		}
	}
}
