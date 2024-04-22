package com.moabam.api.application.member;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.dto.member.ModifyMemberRequest;
import com.moabam.global.common.util.BaseDataCode;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberWriteService {

	private final MemberRepository memberRepository;
	private final ItemRepository itemRepository;
	private final InventoryRepository inventoryRepository;
	private final ParticipantSearchRepository participantSearchRepository;

	private final ClockHolder clockHolder;

	public Member signUp(Long socialId) {
		Member savedMember = memberRepository.save(MemberMapper.toMember(socialId));
		saveMyEgg(savedMember);

		return savedMember;
	}

	public void delete(Member member) {
		member.delete(clockHolder.times());
		memberRepository.delete(member);
		memberRepository.flush();
	}

	public void softDelete(Member member) {
		member.delete(clockHolder.times());
		memberRepository.flush();
	}

	public void changeInfo(Member member, ModifyMemberRequest modifyMemberRequest, String newProfileUri) {
		boolean nickNameChanged = member.changeNickName(modifyMemberRequest.nickname());
		member.changeIntro(modifyMemberRequest.intro());
		member.changeProfileUri(newProfileUri);
		memberRepository.save(member);

		if (nickNameChanged) {
			changeNickname(member.getId(), modifyMemberRequest.nickname());
		}
	}

	private void saveMyEgg(Member member) {
		List<Item> items = getBasicEggs();
		List<Inventory> inventories = items.stream()
			.map(item -> MemberMapper.toInventory(member.getId(), item))
			.toList();
		inventoryRepository.saveAll(inventories);
	}

	private List<Item> getBasicEggs() {
		List<Item> items = itemRepository.findAllById(List.of(BaseDataCode.MORNING_EGG, BaseDataCode.NIGHT_EGG));

		if (items.isEmpty()) {
			throw new BadRequestException(BASIC_SKIN_NOT_FOUND);
		}

		return items;
	}

	private void changeNickname(Long memberId, String changedName) {
		List<Participant> participants = participantSearchRepository.findAllRoomMangerByMemberId(memberId);

		for (Participant participant : participants) {
			participant.getRoom().changeManagerNickname(changedName);
		}
	}
}
