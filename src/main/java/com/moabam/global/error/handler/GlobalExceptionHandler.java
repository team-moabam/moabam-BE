package com.moabam.global.error.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.FcmException;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.global.error.model.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	protected ErrorResponse handleNotFoundException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(UnauthorizedException.class)
	protected ErrorResponse handleUnauthorizedException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ForbiddenException.class)
	protected ErrorResponse handleForbiddenException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ConflictException.class)
	protected ErrorResponse handleConflictException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BadRequestException.class)
	protected ErrorResponse handleBadRequestException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(FcmException.class)
	protected ErrorResponse handleFirebaseException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(NullPointerException.class)
	protected ErrorResponse handleFirebaseException(NullPointerException exception) {
		return new ErrorResponse(exception.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		Map<String, String> validation = new HashMap<>();

		for (FieldError fieldError : fieldErrors) {
			validation.put(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return new ErrorResponse(ErrorMessage.INVALID_REQUEST_FIELD.getMessage(), validation);
	}
}
