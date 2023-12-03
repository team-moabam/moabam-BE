package com.moabam.global.error.handler;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.moabam.api.infrastructure.slack.SlackService;
import com.moabam.global.error.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SlackExceptionHandler {

	private final SlackService slackService;

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	protected ErrorResponse handleException(HttpServletRequest request, Exception exception) throws Exception {
		slackService.send(request, exception);

		return new ErrorResponse(exception.getMessage(), null);
	}
}
