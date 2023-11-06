package com.moabam.api.domain.repository;

import java.util.ArrayList;
import java.util.List;

import com.moabam.api.domain.entity.Certification;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.dto.CertificationImageResponse;
import com.moabam.api.dto.TodayCertificateRankResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CertificationsMapper {

	public static List<CertificationImageResponse> toCertificateImageResponses(Long memberId,
		List<Certification> certifications) {
		List<CertificationImageResponse> cftImageResponses = new ArrayList<>();
		List<Certification> filteredCertifications = certifications.stream()
			.filter(certification -> certification.getMemberId().equals(memberId))
			.toList();

		for (Certification certification : filteredCertifications) {
			CertificationImageResponse cftImageResponse = CertificationImageResponse.builder()
				.routineId(certification.getRoutine().getId())
				.image(certification.getImage())
				.build();

			cftImageResponses.add(cftImageResponse);
		}

		return cftImageResponses;
	}

	public static TodayCertificateRankResponse toTodayCertificateRankResponse(int rank, Member member,
		int contributionPoint, String awakeImage, String sleepImage,
		List<CertificationImageResponse> certificationImageResponses) {
		return TodayCertificateRankResponse.builder()
			.rank(rank)
			.memberId(member.getId())
			.nickname(member.getNickname())
			.profileImage(member.getProfileImage())
			.contributionPoint(contributionPoint)
			.awakeImage(awakeImage)
			.sleepImage(sleepImage)
			.certificationImage(certificationImageResponses)
			.build();
	}
}
