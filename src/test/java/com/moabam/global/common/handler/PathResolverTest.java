package com.moabam.global.common.handler;

import static com.moabam.api.domain.member.Role.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.auth.handler.PathResolver;

class PathResolverTest {

	@DisplayName("path 기본 생성 성공")
	@Test
	void create_basic_path_success() {
		// given
		PathResolver.Path path = PathResolver.Path.builder()
			.uri("/")
			.build();

		assertAll(
			() -> assertThat(path.uri()).isEqualTo("/"),
			() -> assertThat(path.roles()).contains(USER),
			() -> assertThat(path.httpMethods()).contains(GET, PUT, DELETE, POST, PATCH)
		);
	}

	@DisplayName("method직접 설정 생성 성공")
	@Test
	void create_custom_mehtod_path_success() {
		// given
		PathResolver.Path path = PathResolver.Path.builder()
			.uri("/")
			.httpMethod(GET)
			.httpMethods(List.of(POST, DELETE))
			.build();

		assertAll(
			() -> assertThat(path.uri()).isEqualTo("/"),
			() -> assertThat(path.roles()).contains(USER),
			() -> assertThat(path.httpMethods()).contains(GET, DELETE, POST)
		);
	}

	@DisplayName("role직접 설정 생성 성공")
	@Test
	void create_role_mehtod_path_success() {
		// given
		PathResolver.Path path = PathResolver.Path.builder()
			.uri("/")
			.role(USER)
			.roles(List.of(BLACK))
			.build();

		assertAll(
			() -> assertThat(path.uri()).isEqualTo("/"),
			() -> assertThat(path.roles()).contains(USER, BLACK),
			() -> assertThat(path.httpMethods()).contains(GET, PUT, DELETE, POST, PATCH)
		);
	}
}
