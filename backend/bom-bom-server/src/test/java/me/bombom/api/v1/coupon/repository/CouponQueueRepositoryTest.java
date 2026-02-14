package me.bombom.api.v1.coupon.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataRedisTest
@Testcontainers
@ContextConfiguration(classes = CouponQueueRepositoryTest.RedisTestApplication.class)
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
@Import(CouponQueueRepository.class)
class CouponQueueRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class RedisTestApplication {
    }

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired
    CouponQueueRepository couponQueueRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void 발급_완료된_항목은_스킵하고_큐_승격은_원자적으로_동작() {
        String couponName = "day1-coupon";
        long now = System.currentTimeMillis();

        couponQueueRepository.addIfAbsentQueue(couponName, 1L, now + 1);
        couponQueueRepository.addIfAbsentQueue(couponName, 2L, now + 2);
        couponQueueRepository.addIfAbsentQueue(couponName, 3L, now + 3);
        couponQueueRepository.addIfAbsentQueue(couponName, 4L, now + 4);
        couponQueueRepository.addIfAbsentQueue(couponName, 5L, now + 5);

        couponQueueRepository.addIssued(couponName, 2L);

        long expireAt = now + 30_000;
        long promoted = couponQueueRepository.promoteQueueToActive(couponName, 3, expireAt);

        assertThat(promoted).isEqualTo(3);
        assertThat(couponQueueRepository.isActive(couponName, 1L)).isTrue();
        assertThat(couponQueueRepository.isActive(couponName, 3L)).isTrue();
        assertThat(couponQueueRepository.isActive(couponName, 4L)).isTrue();
        assertThat(couponQueueRepository.isActive(couponName, 2L)).isFalse();
        assertThat(couponQueueRepository.isActive(couponName, 5L)).isFalse();

        assertThat(couponQueueRepository.rankQueue(couponName, 1L)).isNull();
        assertThat(couponQueueRepository.rankQueue(couponName, 2L)).isNull();
        assertThat(couponQueueRepository.rankQueue(couponName, 3L)).isNull();
        assertThat(couponQueueRepository.rankQueue(couponName, 4L)).isNull();
        assertThat(couponQueueRepository.rankQueue(couponName, 5L)).isEqualTo(0L);
    }

    @Test
    void 승격_대기열이_비어있으면_0명() {
        // given
        String couponName = "empty-queue";

        // when
        long promoted = couponQueueRepository.promoteQueueToActive(couponName, 3, System.currentTimeMillis());

        // then
        assertThat(promoted).isEqualTo(0L);
    }

    @Test
    void 승격_요청수가_0이면_승격없음() {
        // given
        String couponName = "zero-count";
        couponQueueRepository.addIfAbsentQueue(couponName, 1L, 1.0);

        // when
        long promoted = couponQueueRepository.promoteQueueToActive(couponName, 0, System.currentTimeMillis());

        // then
        assertThat(promoted).isEqualTo(0L);
        assertThat(couponQueueRepository.rankQueue(couponName, 1L)).isEqualTo(0L);
    }

    @Test
    void 승격_요청수가_가용인원_초과면_가능한만큼만() {
        // given
        String couponName = "limited-queue";
        long now = System.currentTimeMillis();

        couponQueueRepository.addIfAbsentQueue(couponName, 1L, now + 1);
        couponQueueRepository.addIfAbsentQueue(couponName, 2L, now + 2);
        couponQueueRepository.addIfAbsentQueue(couponName, 3L, now + 3);
        couponQueueRepository.addIssued(couponName, 2L);
        // when
        long promoted = couponQueueRepository.promoteQueueToActive(couponName, 5, now + 30_000);

        // then
        assertThat(promoted).isEqualTo(2L);
        assertThat(couponQueueRepository.isActive(couponName, 1L)).isTrue();
        assertThat(couponQueueRepository.isActive(couponName, 3L)).isTrue();
        assertThat(couponQueueRepository.isActive(couponName, 2L)).isFalse();
    }

    @Test
    void 대기열_중복등록_막음() {
        // given
        String couponName = "dedupe-queue";

        // when
        boolean first = couponQueueRepository.addIfAbsentQueue(couponName, 1L, 1.0);
        boolean second = couponQueueRepository.addIfAbsentQueue(couponName, 1L, 2.0);

        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    void 대기열_자동등록은_중복요청시_점수키가_증가하지_않음() {
        // given
        String couponName = "dedupe-queue-atomic";

        // when
        boolean first = couponQueueRepository.addIfAbsentQueue(couponName, 1L);
        boolean second = couponQueueRepository.addIfAbsentQueue(couponName, 1L);

        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
        String scoreSequence = redisTemplate.opsForValue().get("coupon:queue:seq:" + couponName);
        assertThat(scoreSequence).isEqualTo("1");
    }

    @Test
    void 대기열_자동점수는_요청순으로_증가() {
        // given
        String couponName = "sequence-score";

        // when
        couponQueueRepository.addIfAbsentQueue(couponName, 1L);
        couponQueueRepository.addIfAbsentQueue(couponName, 2L);
        couponQueueRepository.addIfAbsentQueue(couponName, 3L);

        // then
        assertThat(couponQueueRepository.rankQueue(couponName, 1L)).isEqualTo(0L);
        assertThat(couponQueueRepository.rankQueue(couponName, 2L)).isEqualTo(1L);
        assertThat(couponQueueRepository.rankQueue(couponName, 3L)).isEqualTo(2L);
    }

    @Test
    void 동시_대기열_등록_시_순위가_연속적으로_배정됨() throws Exception {
        // given
        String couponName = "concurrent-queue-sequence";
        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        // when
        for (long memberId = 1L; memberId <= threadCount; memberId++) {
            long finalMemberId = memberId;
            futures.add(executorService.submit(() -> {
                startGate.await();
                couponQueueRepository.addIfAbsentQueue(couponName, finalMemberId);
                return couponQueueRepository.rankQueue(couponName, finalMemberId);
            }));
        }
        startGate.countDown();
        for (Future<Long> future : futures) {
            Long rank = future.get(3, TimeUnit.SECONDS);
            assertThat(rank).isNotNull();
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        Set<Long> rankSet = new HashSet<>();
        for (long memberId = 1L; memberId <= threadCount; memberId++) {
            Long rank = couponQueueRepository.rankQueue(couponName, memberId);
            assertThat(rank).isNotNull();
            rankSet.add(rank);
        }
        assertThat(rankSet).hasSize(threadCount);
        for (long expectedRank = 0L; expectedRank < threadCount; expectedRank++) {
            assertThat(rankSet).contains(expectedRank);
        }
    }

    @Test
    void 액티브_만료자만_삭제() {
        // given
        String couponName = "active-expire";
        long now = System.currentTimeMillis();

        couponQueueRepository.addActive(couponName, 1L, now - 1000);
        couponQueueRepository.addActive(couponName, 2L, now + 1000);
        // when
        long removed = couponQueueRepository.removeExpiredActive(couponName, now);

        // then
        assertThat(removed).isEqualTo(1L);
        assertThat(couponQueueRepository.isActive(couponName, 1L)).isFalse();
        assertThat(couponQueueRepository.isActive(couponName, 2L)).isTrue();
    }

    @Test
    void 액티브_만료시각_조회() {
        // given
        String couponName = "active-expire-score";
        long expireAt = System.currentTimeMillis() + 5000;
        couponQueueRepository.addActive(couponName, 1L, expireAt);

        // when
        Long stored = couponQueueRepository.getActiveExpireAtMillis(couponName, 1L);

        // then
        assertThat(stored).isEqualTo(expireAt);
    }

    @Test
    void 발급카운트_증가_및_조회() {
        // given
        String couponName = "issued-count";

        // when
        long first = couponQueueRepository.increaseIssuedCount(couponName, 1);
        long second = couponQueueRepository.increaseIssuedCount(couponName, 2);

        // then
        assertThat(first).isEqualTo(1L);
        assertThat(second).isEqualTo(3L);
        assertThat(couponQueueRepository.getIssuedCount(couponName)).isEqualTo(3L);
    }

}
