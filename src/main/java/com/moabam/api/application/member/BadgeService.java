package com.moabam.api.application.member;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.member.Badge;
import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.domain.member.repository.BadgeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

	private final BadgeRepository badgeRepository;

	public void createBadge(Long memberId, long certifyCount) {
		Optional<BadgeType> badgeType = BadgeType.getBadgeFrom(certifyCount);

		if (badgeType.isEmpty()
			|| badgeRepository.existsByMemberIdAndType(memberId, badgeType.get())) {
			return;
		}

		Badge badge = MemberMapper.toBadge(memberId, badgeType.get());
		badgeRepository.save(badge);
	}
}
