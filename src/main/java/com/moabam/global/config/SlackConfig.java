package com.moabam.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.gpedro.integrations.slack.SlackApi;

@Configuration
public class SlackConfig {

	@Value("${webhook.slack.url}")
	private String webhookUrl;

	@Bean
	public SlackApi slackApi() {
		return new SlackApi(webhookUrl);
	}
}
