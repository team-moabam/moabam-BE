package com.moabam.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moabam.api.application.auth.mapper.PathMapper;
import com.moabam.global.auth.handler.AuthArgumentResolver;
import com.moabam.global.auth.handler.PathResolver;

@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(handlerMethodArgumentResolver());
	}

	@Bean
	public HandlerMethodArgumentResolver handlerMethodArgumentResolver() {
		return new AuthArgumentResolver();
	}

	@Bean
	public PathResolver pathResolver() {
		PathResolver.Paths path = PathResolver.Paths.builder()
			.permitAll(List.of(
				PathMapper.parsePath("/"),
				PathMapper.pathWithMethod("/members", List.of(HttpMethod.POST)),
				PathMapper.pathWithMethod("/members/login/oauth", List.of(HttpMethod.GET)),
				PathMapper.parsePath("/members/login/*/oauth"),
				PathMapper.parsePath("/admins/login/*/oauth"),
				PathMapper.parsePath("/css/*"),
				PathMapper.parsePath("/js/*"),
				PathMapper.parsePath("/images/*"),
				PathMapper.parsePath("/webjars/*"),
				PathMapper.parsePath("/favicon/*"),
				PathMapper.parsePath("/*/icon-*"),
				PathMapper.parsePath("/favicon.ico"),
				PathMapper.parsePath("/swagger-ui/**"),
				PathMapper.parsePath("/swagger-resources/**"),
				PathMapper.parsePath("/v3/api-docs/**"),
				PathMapper.pathWithMethod("/serverTime", List.of(HttpMethod.GET))))
			.build();

		return new PathResolver(path);
	}
}
