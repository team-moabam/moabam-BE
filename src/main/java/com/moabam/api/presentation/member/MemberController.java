package com.moabam.api.presentation.member;

import com.moabam.api.application.auth.AuthenticationService;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final AuthenticationService authenticationService;

	@GetMapping
	public void socialLogin(HttpServletResponse httpServletResponse) throws IOException {
		String authorizationCodeUri = authenticationService.getAuthorizaionCodeUri();
		httpServletResponse.setContentType(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.SEMI_COLON
			+ StandardCharsets.UTF_8.name());
		httpServletResponse.sendRedirect(authorizationCodeUri);
	}

	@GetMapping("/login/kakao/oauth")
	public void authorizationServerCodeResponse(@ModelAttribute AuthorizationCodeResponse authorizationCodeResponse) {
		if (authorizationCodeResponse.code().isEmpty()) {
			throw new NotFoundException(ErrorMessage.LOGIN_FAILED);
		}
	}
}
