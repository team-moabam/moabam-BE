package com.moabam.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moabam.global.common.handler.CurrentMemberArgumentResolver;
import com.moabam.global.common.util.PathResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";
	private static final String ALLOW_ORIGIN_PATTERN = "[a-z]+\\.moabam.com";
	private static final String ALLOW_LOCAL_HOST = "http://localhost:3000";

	@Override
	public void addCorsMappings(final CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns(ALLOW_ORIGIN_PATTERN, ALLOW_LOCAL_HOST)
			.allowedMethods(ALLOWED_METHOD_NAMES.split(","))
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(handlerMethodArgumentResolver());
	}

	@Bean
	public HandlerMethodArgumentResolver handlerMethodArgumentResolver() {
		return new CurrentMemberArgumentResolver();
	}

	@Bean
	public PathResolver pathResolver() {
		PathResolver.Paths path = PathResolver.Paths.builder()
			.permitOne(
				PathResolver.Path.builder()
					.uri("/members")
					.build()
			)
			.permitOne(
				PathResolver.Path.builder()
					.uri("/members/login/*/oauth")
					.build()
			)
			.build();

		return new PathResolver(path);
	}
}
