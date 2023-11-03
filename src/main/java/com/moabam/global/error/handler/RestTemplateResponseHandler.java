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
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().isError();
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		HttpStatusCode statusCode = response.getStatusCode();

		if (statusCode.is5xxServerError()) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}

		if (statusCode.is4xxClientError()) {
			throw new BadRequestException(ErrorMessage.INVALID_REQUEST_FIELD);
		}
	}
}
