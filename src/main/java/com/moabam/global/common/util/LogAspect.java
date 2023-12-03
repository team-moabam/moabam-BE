package com.moabam.global.common.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class LogAspect {

	@Around("execution(* com.moabam.global.error.handler.GlobalExceptionHandler.*(..))")
	public Object printExceptionLog(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		log.error("===== EXCEPTION LOG =====", (Exception)args[0]);

		return joinPoint.proceed();
	}
}
