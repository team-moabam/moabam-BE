package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.api.domain.repository.MemberSearchRepository;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.LoginResponse;
import com.moabam.api.dto.MemberMapper;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberSearchRepository memberSearchRepository;

	public Member getById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	@Transactional
	public LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Member> member = memberRepository.findBySocialId(authorizationTokenInfoResponse.id());
		Member loginMember = member.orElseGet(() -> signUp(authorizationTokenInfoResponse.id()));

		return MemberMapper.toLoginResponse(loginMember.getId(), member.isEmpty());
	}

	private Member signUp(Long socialId) {
		String randomNickName = createRandomNickName();
		Member member = MemberMapper.toMember(socialId, randomNickName);

		return memberRepository.save(member);
	}

	private String createRandomNickName() {
		return RandomStringUtils.random(6, 0, 0, true, true, null,
			new SecureRandom());
	}

	public Member getManager(Long roomId) {
		return memberSearchRepository.findManager(roomId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}
}
