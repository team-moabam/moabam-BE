package com.moabam.api.presentation;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.AuthenticationService;
import com.moabam.global.common.util.GlobalConstant;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final AuthenticationService authenticationService;

	@GetMapping
	public void socialLogin(HttpServletResponse httpServletResponse) throws IOException {
		String authorizationCodeUri = authenticationService.getAuthorizaionCodeUri();
		httpServletResponse.setContentType(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
		httpServletResponse.sendRedirect(authorizationCodeUri);
	}
}
