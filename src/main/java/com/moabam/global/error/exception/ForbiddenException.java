package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class ForbiddenException extends MoabamException {

	public ForbiddenException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
