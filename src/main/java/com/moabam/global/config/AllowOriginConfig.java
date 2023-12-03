package com.moabam.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "allows")
public record AllowOriginConfig(
        String adminDomain,
        String domain,
        List<String> origin
) {

}
