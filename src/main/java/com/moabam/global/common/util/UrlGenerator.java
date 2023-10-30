package com.moabam.global.common.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlGenerator {

	private static final String QUERY_BEGIN = "?";
	private static final String QUERY_SEPERATE = "&";
	private static final String QUERY_MATCH = "=";

	private String baseUrl;

	private Map<String, String> parameters;

	@Builder
	private UrlGenerator(String baseUrl, @Singular("parameter") Map<String, String> parameters) {
		this.baseUrl = requireNonNull(baseUrl);
		this.parameters = parameters;
	}

	public String generateUrl() {
		StringBuilder url = new StringBuilder(baseUrl);

		url.append(QUERY_BEGIN);
		parameters.forEach((key, value) ->
			url.append(key).append(QUERY_MATCH).append(value).append(QUERY_SEPERATE));
		url.deleteCharAt(url.length() - 1);

		return url.toString();
	}

}
