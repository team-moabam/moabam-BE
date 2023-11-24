package com.moabam.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.image.ImageService;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.image.ImageType;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.ModifyMemberRequest;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final AuthorizationService authorizationService;
	private final MemberService memberService;
	private final ImageService imageService;

	@GetMapping("/login/oauth")
	public void socialLogin(HttpServletResponse httpServletResponse) {
		authorizationService.redirectToLoginPage(httpServletResponse);
	}

	@PostMapping("/login/kakao/oauth")
	@ResponseStatus(HttpStatus.OK)
	public LoginResponse authorizationTokenIssue(@RequestBody AuthorizationCodeResponse authorizationCodeResponse,
		HttpServletResponse httpServletResponse) {
		AuthorizationTokenResponse tokenResponse = authorizationService.requestToken(authorizationCodeResponse);
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse = authorizationService.requestTokenInfo(
			tokenResponse);

		return authorizationService.signUpOrLogin(httpServletResponse, authorizationTokenInfoResponse);
	}

	@GetMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	public void logout(@Auth AuthMember authMember, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {
		authorizationService.logout(authMember, httpServletRequest, httpServletResponse);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public void deleteMember(@Auth AuthMember authMember) {
		authorizationService.unLinkMember(authMember);
	}

	@GetMapping(value = {"", "/{memberId}"})
	public MemberInfoResponse searchInfo(@Auth AuthMember authMember, @PathVariable(required = false) Long memberId) {
		return memberService.searchInfo(authMember, memberId);
	}

	@PostMapping("/modify")
	public void modifyMember(@Auth AuthMember authMember,
		@RequestPart(required = false) ModifyMemberRequest modifyMemberRequest,
		@RequestPart(name = "profileImage", required = false) MultipartFile newProfileImage) {
		String newProfileUri = imageService.uploadImages(List.of(newProfileImage), ImageType.PROFILE_IMAGE).get(0);
		memberService.modifyInfo(authMember, modifyMemberRequest, newProfileUri);
	}
}
