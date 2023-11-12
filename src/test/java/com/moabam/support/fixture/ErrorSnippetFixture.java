package com.moabam.support.fixture;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.springframework.restdocs.snippet.Snippet;

public class ErrorSnippetFixture {

	public static final Snippet ERROR_MESSAGE_RESPONSE = responseFields(
		fieldWithPath("message").type(STRING).description("에러 메시지")
	);
}
