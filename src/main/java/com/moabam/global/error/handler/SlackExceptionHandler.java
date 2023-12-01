package com.moabam.global.error.handler;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.moabam.api.infrastructure.slack.SlackService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SlackExceptionHandler {

	private final SlackService slackService;

	@ExceptionHandler(Exception.class)
	void handleException(HttpServletRequest request, Exception exception) throws Exception {
		slackService.send(request, exception);
		throw exception;
	}
}
