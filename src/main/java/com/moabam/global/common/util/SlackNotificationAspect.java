package com.moabam.global.common.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.moabam.api.infrastructure.slack.SlackService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SlackNotificationAspect {

	private final SlackService slackService;

	@Around("@annotation(com.moabam.global.common.annotation.SlackNotification) && args(request, exception)")
	public Object sendSlack(ProceedingJoinPoint joinPoint, HttpServletRequest request, Exception exception)
		throws Throwable {
		slackService.send(request, exception);

		return joinPoint.proceed();
	}
}
