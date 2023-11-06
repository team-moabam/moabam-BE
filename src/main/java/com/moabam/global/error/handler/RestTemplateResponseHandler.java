package com.moabam.global.error.handler;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

@Component
public class RestTemplateResponseHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse response) {
		try {
			return response.getStatusCode().isError();
		} catch (IOException ioException) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	@Override
	public void handleError(ClientHttpResponse response) {
		try {
			HttpStatusCode statusCode = response.getStatusCode();
			validResponse(statusCode);
		} catch (IOException ioException) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	private void validResponse(HttpStatusCode statusCode) {
		if (statusCode.is5xxServerError()) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}

		if (statusCode.is4xxClientError()) {
			throw new BadRequestException(ErrorMessage.INVALID_REQUEST_FIELD);
		}
	}
}
