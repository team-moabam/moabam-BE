package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class UnauthorizedException extends MoabamException {

	public UnauthorizedException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
