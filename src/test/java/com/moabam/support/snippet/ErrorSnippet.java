package com.moabam.support.snippet;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.springframework.restdocs.snippet.Snippet;

public class ErrorSnippet {

	public static final Snippet ERROR_MESSAGE_RESPONSE = responseFields(
		fieldWithPath("message").type(STRING).description("에러 메시지")
	);
}
