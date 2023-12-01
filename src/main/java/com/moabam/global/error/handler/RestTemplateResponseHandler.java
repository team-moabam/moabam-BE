package com.moabam.global.error.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
		try {
			return response.getStatusCode().isError();
		} catch (IOException ioException) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	@Override
	public void handleError(ClientHttpResponse response) {
		try {
			String errorMessage = parseErrorMessage(response);
			HttpStatusCode statusCode = response.getStatusCode();

			validResponse(statusCode);
		} catch (IOException ioException) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	private String parseErrorMessage(ClientHttpResponse response) throws IOException {
		BufferedReader errorMessage = new BufferedReader(new InputStreamReader(response.getBody()));

		String line = errorMessage.readLine();
		StringBuilder sb = new StringBuilder();

		while (line != null) {
			sb.append(line).append("\n");
			line = errorMessage.readLine();
		}

		return sb.toString();
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
