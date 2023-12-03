package com.moabam.api.presentation;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.MemberFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class RankingControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@BeforeEach
	void init() {
		redisTemplate.delete("Ranking");
	}

	@DisplayName("")
	@WithMember
	@Test
	void top_ranking() throws Exception {
		// given
		List<Member> members = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Member member = MemberFixture.member(String.valueOf(i + 1));
			members.add(member);

			RankingInfo rankingInfo = new RankingInfo((long)(i + 1), member.getNickname(), member.getProfileImage());
			redisTemplate.opsForZSet().add("Ranking", rankingInfo, i + 1);
		}
		memberRepository.saveAll(members);

		RankingInfo rankingInfo = new RankingInfo(21L, "Hello22", "123");
		redisTemplate.opsForZSet().add("Ranking", rankingInfo, 20);
		RankingInfo rankingInfo2 = new RankingInfo(22L, "Hello23", "123");
		redisTemplate.opsForZSet().add("Ranking", rankingInfo2, 19);

		UpdateRanking myRanking = UpdateRanking.builder()
			.score(1L)
			.rankingInfo(RankingInfo.builder()
				.nickname(members.get(0).getNickname())
				.memberId(members.get(0).getId())
				.image(members.get(0).getProfileImage()).build())
			.build();

		// when
		mockMvc.perform(MockMvcRequestBuilders.get("/rankings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.topRankings", hasSize(10)))
			.andExpect(jsonPath("$.myRanking.nickname", is(members.get(0).getNickname())))
			.andExpect(jsonPath("$.myRanking.rank", is(22)));

		// then

	}

}
