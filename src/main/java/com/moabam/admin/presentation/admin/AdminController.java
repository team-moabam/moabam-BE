package com.moabam.admin.presentation.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.admin.application.admin.AdminService;
import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

	private final AuthorizationService authorizationService;
	private final AdminService adminService;

	@PostMapping("/login/kakao/oauth")
	@ResponseStatus(HttpStatus.OK)
	public LoginResponse authorizationTokenIssue(@RequestBody AuthorizationCodeResponse authorizationCodeResponse,
		HttpServletResponse httpServletResponse) {
		adminService.validate(authorizationCodeResponse.state());
		AuthorizationTokenResponse tokenResponse = authorizationService.requestAdminToken(authorizationCodeResponse);
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse =
			authorizationService.requestTokenInfo(tokenResponse);
		LoginResponse loginResponse = adminService.signUpOrLogin(authorizationTokenInfoResponse);
		authorizationService.issueServiceToken(httpServletResponse, loginResponse.publicClaim());

		return loginResponse;
	}
}
