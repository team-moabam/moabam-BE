package com.moabam.support.common;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.handler.PathResolver;

@ExtendWith({FilterProcessExtension.class})
public class WithoutFilterSupporter {

	@MockBean
	private PathResolver pathResolver;

	@BeforeEach
	void setUpMock() {
		willReturn(Optional.of(PathResolver.Path.builder()
			.uri("/")
			.role(Role.USER)
			.build()))
			.given(pathResolver).permitPathMatch(any());
	}
}
