package com.moabam.admin.application.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.moabam.admin.domain.admin.Admin;
import com.moabam.admin.domain.admin.AdminRepository;
import com.moabam.api.application.auth.AuthorizationService;
import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	@Value("${admin}")
	private String adminLoginKey;

	private final AuthorizationService authorizationService;
	private final AdminRepository adminRepository;

	public void validate(String state) {
		if (!adminLoginKey.equals(state)) {
			throw new BadRequestException(ErrorMessage.LOGIN_FAILED_ADMIN_KEY);
		}
	}

	public LoginResponse signUpOrLogin(HttpServletResponse httpServletResponse,
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		LoginResponse loginResponse = login(authorizationTokenInfoResponse);
		authorizationService.issueServiceToken(httpServletResponse, loginResponse.publicClaim());

		return loginResponse;
	}

	private LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Admin> admin = adminRepository.findBySocialId(String.valueOf(authorizationTokenInfoResponse.id()));
		Admin loginMember = admin.orElseGet(() -> signUp(authorizationTokenInfoResponse.id()));

		return AuthMapper.toLoginResponse(loginMember, admin.isEmpty());
	}

	private Admin signUp(Long socialId) {
		Admin admin = AdminMapper.toAdmin(socialId);

		return adminRepository.save(admin);
	}
}
