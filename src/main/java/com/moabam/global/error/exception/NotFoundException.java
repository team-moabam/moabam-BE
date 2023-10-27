package com.moabam.global.error.exception;

import com.moabam.global.error.model.ErrorMessage;

import lombok.Getter;

@Getter
public class NotFoundException extends MoabamException {

	public NotFoundException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
