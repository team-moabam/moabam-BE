package com.moabam.support.common;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.filter.CorsFilter;
import com.moabam.global.auth.handler.PathResolver;

@Import(DataCleanResolver.class)
@ExtendWith({FilterProcessExtension.class, ClearDataExtension.class})
public class WithoutFilterSupporter {

	@MockBean
	private PathResolver pathResolver;

	@SpyBean
	private CorsFilter corsFilter;

	@BeforeEach
	void setUpMock() {
		willReturn("http://localhost:8080")
			.given(corsFilter).getReferer(any());

		willReturn(Optional.of(PathResolver.Path.builder()
			.uri("/")
			.role(Role.USER)
			.build()))
			.given(pathResolver).permitPathMatch(any());
	}
}
