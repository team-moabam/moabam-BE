package com.moabam.admin.application.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.admin.domain.admin.Admin;
import com.moabam.admin.domain.admin.AdminRepository;
import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

	@Value("${admin}")
	private String adminLoginKey;

	private final AdminRepository adminRepository;

	public void validate(String state) {
		if (!adminLoginKey.equals(state)) {
			throw new BadRequestException(ErrorMessage.LOGIN_FAILED_ADMIN_KEY);
		}
	}

	@Transactional
	public LoginResponse signUpOrLogin(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		return login(authorizationTokenInfoResponse);
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

	public Admin findMember(Long id) {
		return adminRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorMessage.MEMBER_NOT_FOUND));
	}
}
