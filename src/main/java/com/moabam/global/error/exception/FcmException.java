package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

public class FcmException extends MoabamException {

	public FcmException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
