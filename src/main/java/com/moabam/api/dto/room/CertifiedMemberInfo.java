package com.moabam.api.dto.room;

import java.time.LocalDate;

import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Room;

import lombok.Builder;

@Builder
public record CertifiedMemberInfo(
	LocalDate date,
	BugType bugType,
	Room room,
	Member member
) {

}
