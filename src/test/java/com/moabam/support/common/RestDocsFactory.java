package com.moabam.support.common;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcSnippetConfigurer;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;

public class RestDocsFactory {

	public static MockMvcSnippetConfigurer restdocs(RestDocumentationContextProvider restDocumentationContextProvider) {
		return MockMvcRestDocumentation.documentationConfiguration(restDocumentationContextProvider)
			.uris()
			.withScheme("http")
			.withHost("dev-api.moabam.com")
			.withPort(80)
			.and()
			.snippets()
			.withEncoding("UTF-8");
	}

	public static OperationRequestPreprocessor getDocumentRequest() {
		return preprocessRequest(prettyPrint());
	}

	public static OperationResponsePreprocessor getDocumentResponse() {
		return preprocessResponse(prettyPrint());
	}
}
