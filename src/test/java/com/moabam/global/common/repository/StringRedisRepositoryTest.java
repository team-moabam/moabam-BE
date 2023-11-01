package com.moabam.global.common.repository;

// @ActiveProfiles("test")
// @SpringBootTest(classes = {RedisConfig.class, StringRedisRepository.class})
class StringRedisRepositoryTest {
	
	//
	// @Autowired
	// private StringRedisRepository stringRedisRepository;
	//
	// @Autowired
	// private StringRedisTemplate stringRedisTemplate;
	//
	// private List<String> keys = new ArrayList<>();
	//
	// @BeforeEach
	// void setUp() {
	// 	keys.clear();
	// 	keys.add("key");
	// }
	//
	// @AfterEach
	// void cleanRedis() {
	// 	if (!keys.isEmpty()) {
	// 		stringRedisTemplate.delete(keys);
	// 	}
	// }
	//
	// @DisplayName("레디스에 문자열 데이터가 성공적으로 저장될 때, - Void")
	// @Test
	// void string_redis_repository_save() {
	// 	// When
	// 	stringRedisRepository.save(keys.get(0), "value", Duration.ofHours(1));
	//
	// 	// Then
	// 	assertThat(stringRedisTemplate.opsForValue().get(keys.get(0))).isEqualTo("value");
	// }
	//
	// @DisplayName("레디스의 특정 데이터가 성공적으로 삭제될 때, - Void")
	// @Test
	// void string_redis_repository_delete() {
	// 	// Given
	// 	stringRedisRepository.save(keys.get(0), "value", Duration.ofHours(1));
	//
	// 	// When
	// 	stringRedisRepository.delete(keys.get(0));
	//
	// 	// Then
	// 	assertThat(stringRedisTemplate.hasKey(keys.get(0))).isFalse();
	// }
	//
	// @DisplayName("레디스의 특정 데이터가 성공적으로 조회될 때, - String(Value)")
	// @Test
	// void string_redis_repository_get() {
	// 	// Given
	// 	stringRedisRepository.save(keys.get(0), "value", Duration.ofHours(1));
	//
	// 	// When
	// 	String actual = stringRedisRepository.get(keys.get(0));
	//
	// 	// Then
	// 	assertThat(actual).isEqualTo(stringRedisTemplate.opsForValue().get(keys.get(0)));
	// }
	//
	// @DisplayName("레디스의 특정 데이터 존재 여부를 성공적으로 체크할 때, - Boolean")
	// @Test
	// void string_redis_repository_hasKey() {
	// 	// When & Then
	// 	assertThat(stringRedisRepository.hasKey("not found key")).isFalse();
	// }
}
