package com.moabam.api.application.member;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.member.BadgeResponse;
import com.moabam.api.dto.member.MemberInfo;
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

	public static MemberInfoSearchResponse toMemberInfoSearchResponse(List<MemberInfo> memberInfos) {
		MemberInfo infos = memberInfos.get(0);
		List<BadgeType> badgeTypes = memberInfos.stream()
			.map(MemberInfo::badges)
			.filter(Objects::nonNull)
			.toList();

		return MemberInfoSearchResponse.builder()
			.nickname(infos.nickname())
			.profileImage(infos.profileImage())
			.intro(infos.intro())
			.totalCertifyCount(infos.totalCertifyCount())
			.badges(new HashSet<>(badgeTypes))
			.goldenBug(infos.goldenBug())
			.morningBug(infos.morningBug())
			.nightBug(infos.nightBug())
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

	public static Inventory toInventory(Long memberId, Item item) {
		return Inventory.builder()
			.memberId(memberId)
			.item(item)
			.isDefault(true)
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
