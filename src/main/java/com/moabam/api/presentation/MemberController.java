package com.moabam.api.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.AuthenticationService;

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

	@GetMapping
	public void authorizationTokenIssue() {

	}
}
