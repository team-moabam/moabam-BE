package com.moabam.api.presentation.member;

import com.moabam.api.application.auth.AuthenticationService;
import com.moabam.api.dto.auth.AuthorizationCodeIssue;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
		AuthorizationCodeIssue authorizationCodeRequest = authenticationService.authorizaionCodeParams();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.SEMI_COLON
			+ StandardCharsets.UTF_8.name()));
		HttpEntity httpEntity = new HttpEntity<>(authorizationCodeRequest.generateQueryParams(), headers);

		return new RestTemplate().exchange(authenticationService.getUrl(), HttpMethod.GET, httpEntity, String.class);
	}

	@GetMapping("/login")
	public void authorizationServerCodeResponse(@ModelAttribute AuthorizationCodeResponse authorizationCodeResponse) {
		if (authorizationCodeResponse.code().isEmpty()) {
			throw new NotFoundException(ErrorMessage.LOGIN_FAILED);
		}


	}
}
