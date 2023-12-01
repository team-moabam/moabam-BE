package com.moabam.api.application.ranking;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.api.dto.ranking.TopRankingInfo;
import com.moabam.api.dto.ranking.TopRankingResponse;
import com.moabam.api.dto.ranking.UpdateRanking;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;
import com.moabam.global.config.EmbeddedRedisConfig;
import com.moabam.support.fixture.BugFixture;
import com.moabam.support.fixture.MemberFixture;

@SpringBootTest(classes = {EmbeddedRedisConfig.class, RankingService.class, ZSetRedisRepository.class})
public class RankingServiceTest {

	@Autowired
	ZSetRedisRepository zSetRedisRepository;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	RankingService rankingService;

	@BeforeEach
	void init() {
		redisTemplate.delete("Ranking");
	}

	@DisplayName("redis에 추가")
	@Nested
	class Add {

		@DisplayName("성공")
		@Test
		void add_success() {
			// given
			Long totalCertifyCount = 0L;
			RankingInfo rankingInfo = RankingInfo.builder()
				.image("https://image.moabam.com/test")
				.memberId(1L)
				.nickname("nickname")
				.build();

			// when
			rankingService.addRanking(rankingInfo, totalCertifyCount);

			// then
			Double resultDouble = redisTemplate.opsForZSet().score("Ranking", rankingInfo);

			assertAll(() -> assertThat(resultDouble).isNotNull(),
				() -> assertThat(resultDouble).isEqualTo(Double.valueOf(totalCertifyCount)));
		}
	}

	@DisplayName("스코어 업데이트")
	@Nested
	class Update {

		@DisplayName("성공")
		@Test
		void update_success() {
			// given
			Member member = MemberFixture.member("1");
			member.increaseTotalCertifyCount();
			member.increaseTotalCertifyCount();

			Member member1 = MemberFixture.member("2");
			member1.increaseTotalCertifyCount();
			member1.increaseTotalCertifyCount();

			List<Member> members = List.of(member, member1);
			List<UpdateRanking> updateRankings = members.stream().map(MemberMapper::toUpdateRanking).toList();

			// when
			rankingService.updateScores(updateRankings);

			Double resultDouble = redisTemplate.opsForZSet().score("Ranking", updateRankings.get(0).rankingInfo());

			// then
			assertAll(() -> assertThat(resultDouble).isNotNull(),
				() -> assertThat(resultDouble).isEqualTo(Double.valueOf(member.getTotalCertifyCount())));
		}
	}

	@DisplayName("사용자 정보 변경")
	@Nested
	class Change {

		@DisplayName("성공")
		@Test
		void update_success() {
			// given
			Member member = Member.builder().socialId("1").bug(BugFixture.bug()).build();
			member.increaseTotalCertifyCount();
			member.increaseTotalCertifyCount();

			long expect = member.getTotalCertifyCount();
			RankingInfo before = MemberMapper.toRankingInfo(member);
			rankingService.addRanking(before, member.getTotalCertifyCount());

			// when
			member.changeIntro("밥세공기");
			RankingInfo changeInfo = MemberMapper.toRankingInfo(member);
			rankingService.changeInfos(before, changeInfo);

			Double resultDouble = redisTemplate.opsForZSet().score("Ranking", changeInfo);

			// then
			assertAll(() -> assertThat(resultDouble).isNotNull(),
				() -> assertThat(resultDouble).isEqualTo(Double.valueOf(expect)));
		}
	}

	@DisplayName("랭킹 삭제")
	@Nested
	class Delete {

		@DisplayName("성공")
		@Test
		void update_success() {
			// given
			Long totalCertify = 5L;
			Member member = Member.builder().socialId("1").bug(BugFixture.bug()).build();
			member.increaseTotalCertifyCount();
			member.increaseTotalCertifyCount();
			RankingInfo rankingInfo = MemberMapper.toRankingInfo(member);

			rankingService.addRanking(rankingInfo, totalCertify);

			// when
			rankingService.removeRanking(rankingInfo);

			Double resultDouble = redisTemplate.opsForZSet().score("Ranking", rankingInfo);

			// then
			assertThat(resultDouble).isNull();
		}
	}

	@DisplayName("조회")
	@Nested
	class Select {

		@DisplayName("성공")
		@Test
		void test() {
			// given
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(1L, "Hello1", "123"), 1);
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(2L, "Hello2", "123"), 2);
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(3L, "Hello3", "123"), 3);
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(4L, "Hello4", "123"), 4);

			// when
			setSerialize(Object.class);
			Set<ZSetOperations.TypedTuple<Object>> rankings = redisTemplate.opsForZSet()
				.reverseRangeWithScores("Ranking", 0, 2);
			setSerialize(String.class);

			// then
			assertThat(rankings).hasSize(3);
		}

		@DisplayName("일부만 조회 성공")
		@Test
		void search_part() {
			// given
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(1L, "Hello1", "123"), 1);
			redisTemplate.opsForZSet().add("Ranking", new RankingInfo(2L, "Hello2", "123"), 2);

			// when
			setSerialize(Object.class);
			Set<ZSetOperations.TypedTuple<Object>> rankings = redisTemplate.opsForZSet()
				.reverseRangeWithScores("Ranking", 0, 10);
			setSerialize(String.class);

			// then
			assertThat(rankings).hasSize(2);
		}

		private void setSerialize(Class classes) {
			redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(classes));
		}

		@DisplayName("랭킹 조회 성공")
		@Test
		void getTopRankings() {
			// given
			for (int i = 0; i < 20; i++) {
				RankingInfo rankingInfo = new RankingInfo((long)(i + 1), "Hello" + (i + 1), "123");
				redisTemplate.opsForZSet().add("Ranking", rankingInfo, i + 1);
			}
			RankingInfo rankingInfo = new RankingInfo(21L, "Hello22", "123");
			redisTemplate.opsForZSet().add("Ranking", rankingInfo, 20);
			RankingInfo rankingInfo2 = new RankingInfo(22L, "Hello23", "123");
			redisTemplate.opsForZSet().add("Ranking", rankingInfo2, 19);

			UpdateRanking myRanking = UpdateRanking.builder()
				.score(1L)
				.rankingInfo(RankingInfo.builder().nickname("Hello1").memberId(1L).image("123").build())
				.build();
			// When
			TopRankingResponse topRankingResponse = rankingService.getMemberRanking(myRanking);

			// Then
			List<TopRankingInfo> topRankings = topRankingResponse.topRankings();
			TopRankingInfo myRank = topRankingResponse.myRanking();
			assertAll(() -> assertThat(topRankings).hasSize(10), () -> assertThat(myRank.score()).isEqualTo(1),
				() -> assertThat(topRankings.get(0).rank()).isEqualTo(1),
				() -> assertThat(topRankings.get(1).rank()).isEqualTo(1),
				() -> assertThat(topRankings.get(2).rank()).isEqualTo(2),
				() -> assertThat(topRankings.get(3).rank()).isEqualTo(2),
				() -> assertThat(topRankings.get(4).rank()).isEqualTo(3));

		}
	}
}
