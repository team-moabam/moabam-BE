package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class BadRequestException extends MoabamException {

	public BadRequestException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
