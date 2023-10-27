package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class MoabamException extends RuntimeException {

	private final ErrorMessage errorMessage;

	public MoabamException(ErrorMessage errorMessage) {
		super(errorMessage.getMessage());
		this.errorMessage = errorMessage;
	}
}
