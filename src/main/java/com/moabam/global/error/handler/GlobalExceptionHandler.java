package com.moabam.global.error.handler;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.FcmException;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.exception.TossPaymentException;
import com.moabam.global.error.exception.UnauthorizedException;
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

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({
		FcmException.class,
		TossPaymentException.class
	})
	protected ErrorResponse handleFcmException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MoabamException.class)
	protected ErrorResponse handleMoabamException(MoabamException moabamException) {
		return new ErrorResponse(moabamException.getMessage(), null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(NullPointerException.class)
	protected ErrorResponse handleNullPointerException(NullPointerException exception) {
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

		return new ErrorResponse(INVALID_REQUEST_FIELD.getMessage(), validation);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
		String typeName = Optional.ofNullable(exception.getRequiredType())
			.map(Class::getSimpleName)
			.orElse("");
		String message = String.format(INVALID_REQUEST_VALUE_TYPE_FORMAT.getMessage(), exception.getValue(), typeName);

		return new ErrorResponse(message, null);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMaxSizeException(MaxUploadSizeExceededException exception) {
		String message = String.format(S3_INVALID_IMAGE_SIZE.getMessage());

		return new ErrorResponse(message, null);
	}
}
