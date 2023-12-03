package com.moabam.api.application.member;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.application.ranking.RankingService;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.api.dto.member.MemberInfoResponse;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.api.dto.member.ModifyMemberRequest;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.common.util.BaseDataCode;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final RankingService rankingService;
	private final FcmService fcmService;
	private final MemberRepository memberRepository;
	private final InventoryRepository inventoryRepository;
	private final ItemRepository itemRepository;
	private final MemberSearchRepository memberSearchRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final ParticipantRepository participantRepository;
	private final ClockHolder clockHolder;

	public Member findMember(Long memberId) {
		return memberSearchRepository.findMember(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	@Transactional
	public LoginResponse login(AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		Optional<Member> member = memberRepository.findBySocialId(String.valueOf(authorizationTokenInfoResponse.id()));
		Member loginMember = member.orElseGet(() -> signUp(authorizationTokenInfoResponse.id()));

		return AuthMapper.toLoginResponse(loginMember, member.isEmpty());
	}

	public List<Member> getRoomMembers(List<Long> memberIds) {
		return memberRepository.findAllById(memberIds);
	}

	@Transactional
	public Member findMemberToDelete(Long memberId) {
		return memberSearchRepository.findMemberNotManager(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	@Transactional
	public void delete(Member member) {
		List<Participant> participants = participantSearchRepository.findAllByMemberIdParticipant(member.getId());

		if (!participants.isEmpty()) {
			throw new BadRequestException(NEED_TO_EXIT_ALL_ROOMS);
		}

		member.delete(clockHolder.times());
		memberRepository.flush();
		memberRepository.delete(member);
		rankingService.removeRanking(MemberMapper.toRankingInfo(member));
		fcmService.deleteTokenByMemberId(member.getId());
	}

	public MemberInfoResponse searchInfo(AuthMember authMember, Long memberId) {
		Long searchId = authMember.id();
		boolean isMe = confirmMe(searchId, memberId);

		if (!isMe) {
			searchId = memberId;
		}
		MemberInfoSearchResponse memberInfoSearchResponse = findMemberInfo(searchId, isMe);

		return MemberMapper.toMemberInfoResponse(memberInfoSearchResponse);
	}

	@Transactional
	public void modifyInfo(AuthMember authMember, ModifyMemberRequest modifyMemberRequest, String newProfileUri) {
		validateNickname(modifyMemberRequest.nickname());
		Member member = memberSearchRepository.findMember(authMember.id())
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		RankingInfo beforeInfo = MemberMapper.toRankingInfo(member);
		member.changeNickName(modifyMemberRequest.nickname());

		boolean nickNameChanged = member.changeNickName(modifyMemberRequest.nickname());
		member.changeIntro(modifyMemberRequest.intro());
		member.changeProfileUri(newProfileUri);
		memberRepository.save(member);

		RankingInfo afterInfo = MemberMapper.toRankingInfo(member);
		rankingService.changeInfos(beforeInfo, afterInfo);

		if (nickNameChanged) {
			changeNickname(authMember.id(), modifyMemberRequest.nickname());
		}
	}

	public UpdateRanking getRankingInfo(AuthMember authMember) {
		Member member = findMember(authMember.id());

		return MemberMapper.toUpdateRanking(member);
	}

	@Scheduled(cron = "0 11 * * * *")
	public void updateAllRanking() {
		List<Member> members = memberSearchRepository.findAllMembers();
		List<UpdateRanking> updateRankings = members.stream()
			.map(MemberMapper::toUpdateRanking)
			.toList();

		rankingService.updateScores(updateRankings);
	}

	private void changeNickname(Long memberId, String changedName) {
		List<Participant> participants = participantSearchRepository.findAllRoomMangerByMemberId(memberId);

		for (Participant participant : participants) {
			participant.getRoom().changeManagerNickname(changedName);
		}
	}

	private void validateNickname(String nickname) {
		if (Objects.isNull(nickname)) {
			return;
		}
		if (StringUtils.isEmpty(nickname) && memberRepository.existsByNickname(nickname)) {
			throw new ConflictException(NICKNAME_CONFLICT);
		}
	}

	private Member signUp(Long socialId) {
		Member member = MemberMapper.toMember(socialId);
		Member savedMember = memberRepository.save(member);
		saveMyEgg(savedMember);
		rankingService.addRanking(MemberMapper.toRankingInfo(member), member.getTotalCertifyCount());

		return savedMember;
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

	private MemberInfoSearchResponse findMemberInfo(Long searchId, boolean isMe) {
		List<MemberInfo> memberInfos = memberSearchRepository.findMemberAndBadges(searchId, isMe);

		if (memberInfos.isEmpty()) {
			throw new BadRequestException(MEMBER_NOT_FOUND);
		}

		return MemberMapper.toMemberInfoSearchResponse(memberInfos);
	}

	private boolean confirmMe(Long myId, Long memberId) {
		return Objects.isNull(memberId) || myId.equals(memberId);
	}
}
