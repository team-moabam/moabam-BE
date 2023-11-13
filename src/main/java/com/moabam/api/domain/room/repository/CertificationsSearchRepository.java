package com.moabam.api.domain.room.repository;

import static com.moabam.api.domain.room.QCertification.*;
import static com.moabam.api.domain.room.QDailyMemberCertification.*;
import static com.moabam.api.domain.room.QDailyRoomCertification.*;
import static com.moabam.api.domain.room.QParticipant.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CertificationsSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Certification> findCertifications(Long roomId, LocalDate date) {
		return jpaQueryFactory.selectFrom(certification)
			.where(
				certification.routine.room.id.eq(roomId),
				certification.createdAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX))
			)
			.fetch();
	}

	public Optional<DailyMemberCertification> findDailyMemberCertification(Long memberId, Long roomId, LocalDate date) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(dailyMemberCertification)
			.where(
				dailyMemberCertification.memberId.eq(memberId),
				dailyMemberCertification.roomId.eq(roomId),
				dailyMemberCertification.createdAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX))
			)
			.fetchOne()
		);
	}

	public List<DailyMemberCertification> findSortedDailyMemberCertifications(Long roomId, LocalDate date) {
		return jpaQueryFactory
			.selectFrom(dailyMemberCertification)
			.join(dailyMemberCertification.participant, participant).fetchJoin()
			.where(
				dailyMemberCertification.roomId.eq(roomId),
				dailyMemberCertification.createdAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX))
			)
			.orderBy(
				dailyMemberCertification.createdAt.asc()
			)
			.fetch();
	}

	public Optional<DailyRoomCertification> findDailyRoomCertification(Long roomId, LocalDate date) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(dailyRoomCertification)
			.where(
				dailyRoomCertification.roomId.eq(roomId),
				dailyRoomCertification.certifiedAt.eq(date)
			)
			.fetchOne());
	}

	public List<DailyRoomCertification> findDailyRoomCertifications(Long roomId, LocalDate date) {
		return jpaQueryFactory
			.selectFrom(dailyRoomCertification)
			.where(
				dailyRoomCertification.roomId.eq(roomId),
				dailyRoomCertification.certifiedAt.eq(date)
			)
			.fetch();
	}
}
