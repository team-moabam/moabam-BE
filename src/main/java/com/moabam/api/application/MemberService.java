package com.moabam.api.application;

import static com.moabam.global.common.constant.GlobalConstant.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
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

	public boolean isExistMember(Long memberId) {
		return memberRepository.existsById(memberId);
	}

	@Transactional
	public LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Member> member = memberRepository.findBySocialId(authorizationTokenInfoResponse.id());

		if (member.isEmpty()) {
			Member signUpMember = signUp(authorizationTokenInfoResponse.id());
			return MemberMapper.toLoginResponse(signUpMember.getId(), true);
		}

		return MemberMapper.toLoginResponse(member.get().getId());
	}

	private Member signUp(long socialId) {
		String randomNick = createRandomNickName();
		Member member = MemberMapper.toMember(socialId, randomNick);

		return memberRepository.save(member);
	}

	private String createRandomNickName() {
		return RandomStringUtils.randomAlphanumeric(RANDOM_NICKNAME_SIZE) + UNDER_BAR + LocalDate.now();
	}

	public Member getManager(Long roomId) {
		return memberSearchRepository.findManager(roomId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}
}
