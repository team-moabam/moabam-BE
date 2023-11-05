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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CertificationsSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Certification> findCertifications(List<Long> routineIds, LocalDate date) {
		BooleanExpression expression = null;

		for (Long routineId : routineIds) {
			BooleanExpression routineExpression = certification.routine.id.eq(routineId);
			expression = expression == null ? routineExpression : expression.or(routineExpression);
		}

		return jpaQueryFactory
			.selectFrom(certification)
			.where(
				expression,
				certification.createdAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX))
			)
			.fetch();
	}

	public List<DailyMemberCertification> findDailyMemberCertifications(Long roomId, LocalDate date) {
		return jpaQueryFactory
			.selectFrom(dailyMemberCertification)
			.join(dailyMemberCertification.participant, participant).fetchJoin()
			.where(
				dailyMemberCertification.roomId.eq(roomId),
				dailyMemberCertification.createdAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX))
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
