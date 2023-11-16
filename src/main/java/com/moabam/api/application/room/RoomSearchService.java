package com.moabam.api.application.room;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.mapper.CertificationsMapper;
import com.moabam.api.application.room.mapper.RoomMapper;
import com.moabam.api.application.room.mapper.RoutineMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoutineSearchRepository;
import com.moabam.api.dto.room.CertificationImageResponse;
import com.moabam.api.dto.room.MyRoomResponse;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.RoomDetailsResponse;
import com.moabam.api.dto.room.RoutineResponse;
import com.moabam.api.dto.room.TodayCertificateRankResponse;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomSearchService {

	private final CertificationsSearchRepository certificationsSearchRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final RoutineSearchRepository routineSearchRepository;
	private final MemberService memberService;
	private final RoomCertificationService roomCertificationService;

	public RoomDetailsResponse getRoomDetails(Long memberId, Long roomId) {
		LocalDate today = LocalDate.now();
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
		Room room = participant.getRoom();

		String managerNickname = memberService.getManager(roomId).getNickname();
		List<DailyMemberCertification> dailyMemberCertifications =
			certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today);
		List<RoutineResponse> routineResponses = getRoutineResponses(roomId);
		List<TodayCertificateRankResponse> todayCertificateRankResponses = getTodayCertificateRankResponses(roomId,
			dailyMemberCertifications, today);
		List<LocalDate> certifiedDates = getCertifiedDates(roomId, today);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		return RoomMapper.toRoomDetailsResponse(room, managerNickname, routineResponses, certifiedDates,
			todayCertificateRankResponses, completePercentage);
	}

	public MyRoomsResponse getMyRooms(Long memberId) {
		LocalDate today = LocalDate.now();
		List<MyRoomResponse> myRoomResponses = new ArrayList<>();
		List<Participant> participants = participantSearchRepository.findParticipantsByMemberId(memberId);

		for (Participant participant : participants) {
			Room room = participant.getRoom();
			boolean isMemberCertified = roomCertificationService.existsMemberCertification(memberId, room.getId(),
				today);
			boolean isRoomCertified = roomCertificationService.existsRoomCertification(room.getId(), today);

			myRoomResponses.add(RoomMapper.toMyRoomResponse(room, isMemberCertified, isRoomCertified));
		}

		return RoomMapper.toMyRoomsResponse(myRoomResponses);
	}

	private List<RoutineResponse> getRoutineResponses(Long roomId) {
		List<Routine> roomRoutines = routineSearchRepository.findAllByRoomId(roomId);

		return RoutineMapper.toRoutineResponses(roomRoutines);
	}

	private List<TodayCertificateRankResponse> getTodayCertificateRankResponses(Long roomId,
		List<DailyMemberCertification> dailyMemberCertifications, LocalDate today) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		List<Certification> certifications = certificationsSearchRepository.findCertifications(roomId, today);
		List<Participant> participants = participantSearchRepository.findParticipantsByRoomId(roomId);
		List<Member> members = memberService.getRoomMembers(participants.stream()
			.map(Participant::getMemberId)
			.toList());

		addCompletedMembers(responses, dailyMemberCertifications, members, certifications, participants, today);
		addUncompletedMembers(responses, dailyMemberCertifications, members, participants, today);

		return responses;
	}

	private void addCompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Certification> certifications, List<Participant> participants, LocalDate today) {

		int rank = 1;

		for (DailyMemberCertification certification : dailyMemberCertifications) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(certification.getMemberId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			int contributionPoint = calculateContributionPoint(member.getId(), participants, today);
			List<CertificationImageResponse> certificationImageResponses =
				CertificationsMapper.toCertificateImageResponses(member.getId(), certifications);

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				rank, member, contributionPoint, "https://~awake", "https://~sleep", certificationImageResponses);

			rank += 1;
			responses.add(response);
		}
	}

	private void addUncompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Participant> participants, LocalDate today) {

		List<Long> allMemberIds = participants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.toList());

		List<Long> certifiedMemberIds = dailyMemberCertifications.stream()
			.map(DailyMemberCertification::getMemberId)
			.toList();

		allMemberIds.removeAll(certifiedMemberIds);

		for (Long memberId : allMemberIds) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(memberId))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			int contributionPoint = calculateContributionPoint(memberId, participants, today);

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				500, member, contributionPoint, "https://~awake", "https://~sleep", null);

			responses.add(response);
		}
	}

	private int calculateContributionPoint(Long memberId, List<Participant> participants, LocalDate today) {
		Participant participant = participants.stream()
			.filter(p -> p.getMemberId().equals(memberId))
			.findAny()
			.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

		int participatedDays = Period.between(participant.getCreatedAt().toLocalDate(), today).getDays() + 1;

		return (int)(((double)participant.getCertifyCount() / participatedDays) * 100);
	}

	private List<LocalDate> getCertifiedDates(Long roomId, LocalDate today) {
		List<DailyRoomCertification> certifications = certificationsSearchRepository.findDailyRoomCertifications(
			roomId, today);

		return certifications.stream().map(DailyRoomCertification::getCertifiedAt).toList();
	}

	private double calculateCompletePercentage(int certifiedMembersCount, int currentsMembersCount) {
		double completePercentage = ((double)certifiedMembersCount / currentsMembersCount) * 100;

		return Math.round(completePercentage * 100) / 100.0;
	}
}
