package com.moabam.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moabam.api.application.auth.mapper.PathMapper;
import com.moabam.global.auth.handler.CurrentMemberArgumentResolver;
import com.moabam.global.auth.handler.PathResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final String ALLOWED_METHOD_NAMES = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,PATCH";
	private static final String ALLOW_ORIGIN_PATTERN = "[a-z]+\\.moabam.com";

	@Value("${allow}")
	private String allowLocalHost;

	@Override
	public void addCorsMappings(final CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns(ALLOW_ORIGIN_PATTERN, allowLocalHost)
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
			.permitAll(List.of(
				PathMapper.parsePath("/"),
				PathMapper.parsePath("/members"),
				PathMapper.parsePath("/members/login/*/oauth"),
				PathMapper.parsePath("/css/*"),
				PathMapper.parsePath("/js/*"),
				PathMapper.parsePath("/images/*"),
				PathMapper.parsePath("/webjars/*"),
				PathMapper.parsePath("/favicon/*"),
				PathMapper.parsePath("/*/icon-*")
			))
			.build();

		return new PathResolver(path);
	}
}
