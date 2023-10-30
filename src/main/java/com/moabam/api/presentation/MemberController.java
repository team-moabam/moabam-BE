package com.moabam.api.presentation;

import com.moabam.api.application.auth.AuthenticationService;
import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.global.common.util.GlobalConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final AuthenticationService authenticationService;

	@GetMapping
	public ResponseEntity<String> socialLogin() {
		AuthorizationCodeRequest authorizationCodeRequest = authenticationService.authorizaionCodeParams();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.SEMI_COLON
			+ StandardCharsets.UTF_8.name()));
		HttpEntity httpEntity = new HttpEntity<>(authorizationCodeRequest.generateQueryParams(), headers);

		return new RestTemplate().exchange(authenticationService.getUrl(), HttpMethod.GET, httpEntity, String.class);
	}
}
