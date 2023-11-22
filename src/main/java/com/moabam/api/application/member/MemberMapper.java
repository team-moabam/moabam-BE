package com.moabam.api.application.member;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.member.BadgeResponse;
import com.moabam.api.dto.member.DeleteMemberResponse;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.MemberInfoSearchResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberMapper {

	public static Member toMember(Long socialId) {
		return Member.builder()
			.socialId(String.valueOf(socialId))
			.bug(Bug.builder().build())
			.build();
	}

	public static DeleteMemberResponse toDeleteMemberResponse(Long memberId, String socialId) {
		return DeleteMemberResponse.builder()
			.socialId(socialId)
			.id(memberId)
			.build();
	}

	public static MemberInfoResponse toMemberInfoResponse(MemberInfoSearchResponse memberInfoSearchResponse,
		List<Inventory> inventories) {
		long certifyCount = memberInfoSearchResponse.totalCertifyCount();

		return MemberInfoResponse.builder()
			.nickname(memberInfoSearchResponse.nickname())
			.profileImage(memberInfoSearchResponse.profileImage())
			.intro(memberInfoSearchResponse.intro())
			.level(certifyCount / LEVEL_DIVISOR)
			.exp(certifyCount % LEVEL_DIVISOR)
			.birds(defaultSkins(inventories))
			.badges(badgedNames(memberInfoSearchResponse.badges()))
			.goldenBug(memberInfoSearchResponse.goldenBug())
			.morningBug(memberInfoSearchResponse.morningBug())
			.nightBug(memberInfoSearchResponse.nightBug())
			.build();
	}

	private static List<BadgeResponse> badgedNames(Set<BadgeType> badgeTypes) {
		return BadgeType.memberBadgeMap(badgeTypes);
	}

	private static Map<String, String> defaultSkins(List<Inventory> inventories) {
		return inventories.stream()
			.collect(Collectors.toMap(
				inventory -> inventory.getItem().getType().name(),
				inventory -> inventory.getItem().getImage()
			));
	}
}
