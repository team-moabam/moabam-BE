package com.moabam.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "allows")
public record AllowOriginConfig(
	List<String> origin
) {

}
