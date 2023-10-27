package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class ConflictException extends MoabamException {

	public ConflictException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
