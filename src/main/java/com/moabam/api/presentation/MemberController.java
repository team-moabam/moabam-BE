package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.AuthenticationService;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.api.dto.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final AuthenticationService authenticationService;

	@GetMapping
	public void socialLogin(HttpServletResponse httpServletResponse) {
		authenticationService.redirectToLoginPage(httpServletResponse);
	}

	@GetMapping("/login/kakao/oauth")
	@ResponseStatus(HttpStatus.OK)
	public LoginResponse authorizationTokenIssue(@ModelAttribute AuthorizationCodeResponse authorizationCodeResponse,
		HttpServletResponse httpServletResponse) {
		AuthorizationTokenResponse tokenResponse = authenticationService.requestToken(authorizationCodeResponse);
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			authenticationService.requestTokenInfo(tokenResponse);
		return authenticationService.signUpOrLogin(httpServletResponse, authorizationTokenInfoResponse);
	}
}
