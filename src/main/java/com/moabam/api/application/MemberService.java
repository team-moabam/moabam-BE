package com.moabam.api.application;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	public Member getById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public boolean isEnterRoomAvailable(Long memberId, RoomType roomType) {
		Member member = getById(memberId);

		if (roomType.equals(MORNING) && member.getCurrentMorningCount() >= 3) {
			return false;
		}

		if (roomType.equals(NIGHT) && member.getCurrentNightCount() >= 3) {
			return false;
		}

		return true;
	}

	@Transactional
	public void increaseRoomCount(Long memberId, RoomType roomType) {
		Member member = getById(memberId);

		if (roomType.equals(MORNING)) {
			member.enterMorningRoom();
		}

		member.enterNightRoom();
	}
}
