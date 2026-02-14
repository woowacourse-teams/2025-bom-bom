package me.bombom.api.v1.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@TestPropertySource(properties = {
        "coupon.events[0].name=sync-coupon",
        "coupon.events[0].max-count=3",
        "coupon.events[0].batch-size=50",
        "coupon.events[0].active-limit=10",
        "coupon.events[0].active-ttl-seconds=30",
        "coupon.events[0].polling-interval-seconds=3",
        "coupon.events[0].image-url=https://example.com/sync.png",
        "spring.task.scheduling.enabled=false"
})
class CouponIssueSchedulerTest {

    @Autowired
    private CouponIssueScheduler couponIssueScheduler;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private CouponQueueRepository couponQueueRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // given
        couponIssueRepository.deleteAllInBatch();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void 스케줄러_실행시_DB발급수기준으로_슬롯계산하고_Redis카운트를_보정한다() {
        // given
        couponIssueRepository.save(CouponIssue.of(1001L, "sync-coupon", "https://example.com/sync.png"));
        couponIssueRepository.save(CouponIssue.of(1002L, "sync-coupon", "https://example.com/sync.png"));
        couponIssueRepository.save(CouponIssue.of(1003L, "sync-coupon", "https://example.com/sync.png"));
        couponQueueRepository.increaseIssuedCount("sync-coupon", 0L);

        couponQueueRepository.addIfAbsentQueue("sync-coupon", 2001L, 1.0);
        couponQueueRepository.addIfAbsentQueue("sync-coupon", 2002L, 2.0);
        couponQueueRepository.addIfAbsentQueue("sync-coupon", 2003L, 3.0);

        // when
        couponIssueScheduler.issue();

        // then
        assertThat(couponQueueRepository.getIssuedCount("sync-coupon")).isEqualTo(3L);
        assertThat(couponQueueRepository.getActiveCount("sync-coupon")).isEqualTo(0L);
        assertThat(couponQueueRepository.getQueueCount("sync-coupon")).isEqualTo(3L);
    }
}
