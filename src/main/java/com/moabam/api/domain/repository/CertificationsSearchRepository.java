package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QCertification.*;
import static com.moabam.api.domain.entity.QDailyMemberCertification.*;
import static com.moabam.api.domain.entity.QDailyRoomCertification.*;
import static com.moabam.api.domain.entity.QParticipant.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Certification;
import com.moabam.api.domain.entity.DailyMemberCertification;
import com.moabam.api.domain.entity.DailyRoomCertification;
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
