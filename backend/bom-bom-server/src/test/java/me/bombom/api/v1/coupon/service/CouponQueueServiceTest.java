package me.bombom.api.v1.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.exception.CouponErrorReason;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatus;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatusResponse;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@TestPropertySource(properties = {
        "coupon.events[0].name=day1-coupon",
        "coupon.events[0].max-count=3",
        "coupon.events[0].batch-size=50",
        "coupon.events[0].active-limit=2",
        "coupon.events[0].active-ttl-seconds=30",
        "coupon.events[0].polling-interval-seconds=3",
        "coupon.events[0].image-url=https://example.com/coupon.png",
        "coupon.events[1].name=limited-coupon",
        "coupon.events[1].max-count=1",
        "coupon.events[1].batch-size=50",
        "coupon.events[1].active-limit=1",
        "coupon.events[1].active-ttl-seconds=30",
        "coupon.events[1].polling-interval-seconds=3",
        "coupon.events[1].image-url=https://example.com/limited.png",
        "coupon.events[2].name=ended-coupon",
        "coupon.events[2].max-count=1",
        "coupon.events[2].batch-size=50",
        "coupon.events[2].active-limit=1",
        "coupon.events[2].active-ttl-seconds=30",
        "coupon.events[2].polling-interval-seconds=3",
        "coupon.events[2].image-url=https://example.com/ended.png",
        "coupon.events[2].end-at=2000-01-01T00:00:00",
        "coupon.events[3].name=demo-coupon",
        "coupon.events[3].max-count=10",
        "coupon.events[3].batch-size=50",
        "coupon.events[3].active-limit=10",
        "coupon.events[3].active-ttl-seconds=30",
        "coupon.events[3].polling-interval-seconds=3",
        "coupon.events[3].image-url=https://example.com/demo.png",
        "coupon.events[4].name=scenario-coupon",
        "coupon.events[4].max-count=3",
        "coupon.events[4].batch-size=50",
        "coupon.events[4].active-limit=6",
        "coupon.events[4].active-ttl-seconds=30",
        "coupon.events[4].polling-interval-seconds=3",
        "coupon.events[4].image-url=https://example.com/scenario.png",
        "coupon.events[5].name=future-coupon",
        "coupon.events[5].max-count=1",
        "coupon.events[5].batch-size=50",
        "coupon.events[5].active-limit=1",
        "coupon.events[5].active-ttl-seconds=30",
        "coupon.events[5].polling-interval-seconds=3",
        "coupon.events[5].image-url=https://example.com/future.png",
        "coupon.events[5].start-at=2999-01-01T00:00:00",
        "spring.task.scheduling.enabled=false"
})
class CouponQueueServiceTest {
    private static final Logger log = LoggerFactory.getLogger(CouponQueueServiceTest.class);

    @Autowired
    private CouponQueueService couponQueueService;

    @Autowired
    private CouponQueueRepository couponQueueRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Member member;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        member = TestFixture.createUniqueMember("member-1", "provider-1");
        memberRepository.save(member);

        couponIssueRepository.saveAll(TestFixture.createCouponPool("day1-coupon", 3, "https://example.com/coupon.png"));
        couponIssueRepository.saveAll(TestFixture.createCouponPool("limited-coupon", 1, "https://example.com/limited.png"));
        couponIssueRepository.saveAll(TestFixture.createCouponPool("ended-coupon", 1, "https://example.com/ended.png"));
        couponIssueRepository.saveAll(TestFixture.createCouponPool("demo-coupon", 10, "https://example.com/demo.png"));
        couponIssueRepository.saveAll(TestFixture.createCouponPool("scenario-coupon", 3, "https://example.com/scenario.png"));
    }

    @Test
    void 대기열_등록_성공() {
        // given
        // when
        CouponQueueStatusResponse response = couponQueueService.registerQueue("day1-coupon", member);
        // then
        Long rank = couponQueueRepository.rankQueue("day1-coupon", member.getId());
        boolean isWaiting = response.status() == CouponQueueStatus.WAITING;
        boolean isActive = response.status() == CouponQueueStatus.ACTIVE;
        assertThat(isWaiting || isActive).isTrue();
        if (isWaiting) {
            assertThat(rank).isEqualTo(0L);
        } else {
            assertThat(couponQueueRepository.isActive("day1-coupon", member.getId())).isTrue();
        }
    }

    @Test
    void 대기열_등록_이벤트시작전_예외() {
        // given
        // when, then
        assertThatThrownBy(() -> couponQueueService.registerQueue("future-coupon", member))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 대기열_등록_발급완료_사용자면_ISSUED() {
        // given
        couponIssueRepository.save(CouponIssue.of(member.getId(), "day1-coupon", "https://example.com/coupon.png"));
        // when
        CouponQueueStatusResponse response = couponQueueService.registerQueue("day1-coupon", member);
        // then
        assertThat(response.status()).isEqualTo(CouponQueueStatus.ISSUED);
    }

    @Test
    void 대기열_조회_대기중이면_순번반환() {
        // given
        couponQueueService.registerQueue("day1-coupon", member);
        // when
        CouponQueueStatusResponse response = couponQueueService.getQueueStatus("day1-coupon", member);
        // then
        assertThat(response.status()).isEqualTo(CouponQueueStatus.WAITING);
        assertThat(response.position()).isEqualTo(1L);
    }

    @Test
    void 대기열_조회_입장허용이면_TTL반환() {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("day1-coupon", member.getId(), expireAt);
        // when
        CouponQueueStatusResponse response = couponQueueService.getQueueStatus("day1-coupon", member);
        // then
        assertThat(response.status()).isEqualTo(CouponQueueStatus.ACTIVE);
        assertThat(response.activeExpiresInSeconds()).isNotNull();
    }

    @Test
    void 대기열_조회_발급완료면_ISSUED() {
        // given
        couponIssueRepository.save(CouponIssue.of(member.getId(), "day1-coupon", "https://example.com/coupon.png"));
        // when
        CouponQueueStatusResponse response = couponQueueService.getQueueStatus("day1-coupon", member);
        // then
        assertThat(response.status()).isEqualTo(CouponQueueStatus.ISSUED);
    }

    @Test
    void 발급_확정_성공시_DB저장및Redis반영() {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("day1-coupon", member.getId(), expireAt);
        // when
        var response = couponQueueService.issueCoupon("day1-coupon", member);
        // then
        assertThat(response.imageUrl()).isEqualTo("https://example.com/coupon.png");
        assertThat(response.issuedAt()).isNotNull();
        assertThat(couponIssueRepository.existsByMemberIdAndCouponName(member.getId(), "day1-coupon"))
                .isTrue();
        assertThat(couponQueueRepository.isActive("day1-coupon", member.getId())).isFalse();
        assertThat(couponQueueRepository.getIssuedCount("day1-coupon")).isEqualTo(1L);
    }

    @Test
    void 발급_확정_입장허용아니면_예외() {
        // given
        // when, then
        Throwable thrown = catchThrowable(() -> couponQueueService.issueCoupon("day1-coupon", member));
        assertThat(thrown).isInstanceOf(CIllegalArgumentException.class);
        CIllegalArgumentException exception = (CIllegalArgumentException) thrown;
        assertThat(exception.getContext())
                .containsEntry("reason", CouponErrorReason.NOT_ACTIVE_SLOT.name());
    }

    @Test
    void 발급_확정_중복요청이면_예외() {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("day1-coupon", member.getId(), expireAt);
        // when
        couponQueueService.issueCoupon("day1-coupon", member);
        // then
        couponQueueRepository.addActive("day1-coupon", member.getId(), System.currentTimeMillis() + 30_000);
        Throwable duplicate = catchThrowable(() -> couponQueueService.issueCoupon("day1-coupon", member));
        assertThat(duplicate).isInstanceOf(CIllegalArgumentException.class);
        assertThat(((CIllegalArgumentException) duplicate).getContext())
                .containsEntry("reason", CouponErrorReason.DUPLICATED_REQUEST.name());
        assertThat(couponQueueRepository.getIssuedCount("day1-coupon")).isEqualTo(1L);
    }

    @Test
    void 발급_확정_최대수량초과면_예외() {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("limited-coupon", member.getId(), expireAt);
        // when
        couponQueueService.issueCoupon("limited-coupon", member);
        Member other = TestFixture.createUniqueMember("member-2", "provider-2");
        memberRepository.save(other);
        couponQueueRepository.addActive("limited-coupon", other.getId(), System.currentTimeMillis() + 30_000);
        // then
        Throwable soldOut = catchThrowable(() -> couponQueueService.issueCoupon("limited-coupon", other));
        assertThat(soldOut).isInstanceOf(CIllegalArgumentException.class);
        assertThat(((CIllegalArgumentException) soldOut).getContext())
                .containsEntry("reason", CouponErrorReason.SOLD_OUT.name());
    }

    @Test
    void 대기열_조회_소진이면_SOLD_OUT() {
        // given
        couponQueueRepository.addActive("limited-coupon", member.getId(), System.currentTimeMillis() + 30_000);
        couponQueueService.issueCoupon("limited-coupon", member);
        Member other = TestFixture.createUniqueMember("member-2", "provider-2");
        memberRepository.save(other);

        // when
        CouponQueueStatusResponse response = couponQueueService.getQueueStatus("limited-coupon", other);

        // then
        assertThat(response.status()).isEqualTo(CouponQueueStatus.SOLD_OUT);
    }

    @Test
    void 발급_확정_소진이면_ACTIVE정리후_예외() {
        // given
        couponQueueRepository.addActive("limited-coupon", member.getId(), System.currentTimeMillis() + 30_000);
        couponQueueService.issueCoupon("limited-coupon", member);
        Member other = TestFixture.createUniqueMember("member-3", "provider-3");
        memberRepository.save(other);
        couponQueueRepository.addIfAbsentQueue("limited-coupon", other.getId(), System.currentTimeMillis());
        couponQueueRepository.addActive("limited-coupon", other.getId(), System.currentTimeMillis() + 30_000);

        // when, then
        assertThatThrownBy(() -> couponQueueService.issueCoupon("limited-coupon", other))
                .isInstanceOf(CIllegalArgumentException.class);
        assertThat(couponQueueRepository.isActive("limited-coupon", other.getId())).isFalse();
        assertThat(couponQueueRepository.rankQueue("limited-coupon", other.getId())).isNull();
    }

    @Test
    void 발급_확정_이벤트종료후면_예외() {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("ended-coupon", member.getId(), expireAt);
        // when, then
        assertThatThrownBy(() -> couponQueueService.issueCoupon("ended-coupon", member))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 동시_대기열_등록_성공() throws Exception {
        // given
        int threadCount = 30;
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member newMember = TestFixture.createUniqueMember("member-" + (i + 1000), "provider-" + (i + 1000));
            members.add(newMember);
        }
        memberRepository.saveAll(members);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (Member m : members) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    long now = System.currentTimeMillis();
                    couponQueueService.registerQueue("day1-coupon", m);
                    log.info("대기열에 추가 - {} ({}초)", Thread.currentThread().getName(), now);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.warn("대기열 등록 실패 - {}: {}", Thread.currentThread().getName(), e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        // then
        long queueCount = couponQueueRepository.getQueueCount("day1-coupon");
        long activeCount = couponQueueRepository.getActiveCount("day1-coupon");
        assertThat(queueCount + activeCount).isEqualTo(successCount.get());
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }

    @Test
    void 동시_등록_10명_입장_6명_성공_3명_실패() throws Exception {
        // given
        int totalMembers = 10;
        int activeLimit = 6;
        int successLimit = 3;
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < totalMembers; i++) {
            Member newMember = TestFixture.createUniqueMember("scenario-member-" + (i + 3000), "scenario-provider-" + (i + 3000));
            members.add(newMember);
        }
        memberRepository.saveAll(members);
        Map<Long, Member> memberById = members.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        ConcurrentHashMap<Long, String> threadNameByMemberId = new ConcurrentHashMap<>();

        ExecutorService registerPool = Executors.newFixedThreadPool(totalMembers);
        CountDownLatch readyLatch = new CountDownLatch(totalMembers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalMembers);
        ConcurrentHashMap<Long, Long> queueOrderByMemberId = new ConcurrentHashMap<>();

        for (Member m : members) {
            registerPool.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    couponQueueService.registerQueue("scenario-coupon", m);
                    threadNameByMemberId.put(m.getId(), Thread.currentThread().getName());
                    Long rank = couponQueueRepository.rankQueue("scenario-coupon", m.getId());
                    long order = rank != null ? rank + 1 : -1;
                    log.info("대기열에 추가 - {} ({}초)", Thread.currentThread().getName(), System.currentTimeMillis());
                    if (order > 0) {
                        queueOrderByMemberId.put(m.getId(), order);
                    }
                    log.info("'{}'님의 현재 대기열 순번은 {}번 입니다.", Thread.currentThread().getName(), order);
                } catch (Exception e) {
                    log.warn("대기열 등록 실패 - {}: {}", Thread.currentThread().getName(), e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        registerPool.shutdownNow();

        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.promoteQueueToActive("scenario-coupon", activeLimit, expireAt);
        List<Member> activeMembers = couponQueueRepository.rangeActive("scenario-coupon", 0, activeLimit - 1).stream()
                .map(memberById::get)
                .filter(m -> m != null)
                .toList();
        for (Member activeMember : activeMembers) {
            String threadName = threadNameByMemberId.getOrDefault(activeMember.getId(), "unknown-thread");
            Long queueOrder = queueOrderByMemberId.get(activeMember.getId());
            log.info("ACTIVE 승급 - memberId={}, thread={}, 등록순번={}", activeMember.getId(), threadName, queueOrder);
        }

        ExecutorService issuePool = Executors.newFixedThreadPool(activeLimit);
        CountDownLatch issueDone = new CountDownLatch(activeMembers.size());
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (Member target : activeMembers) {
            issuePool.submit(() -> {
                try {
                    var response = couponQueueService.issueCoupon("scenario-coupon", target);
                    log.info("'{}'님의 쿠폰이 발급되었습니다 ({})", Thread.currentThread().getName(), response.issuedAt());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.warn("'{}'님의 쿠폰 발급에 실패했습니다.", Thread.currentThread().getName());
                    failCount.incrementAndGet();
                } finally {
                    issueDone.countDown();
                }
            });
        }
        issueDone.await(10, TimeUnit.SECONDS);
        issuePool.shutdownNow();

        // then
        assertThat(activeMembers.size()).isEqualTo(activeLimit);
        assertThat(successCount.get()).isEqualTo(successLimit);
        assertThat(failCount.get()).isEqualTo(activeLimit - successLimit);
        assertThat(couponIssueRepository.countByCouponNameAndMemberIdIsNotNull("scenario-coupon"))
                .isEqualTo(successLimit);
        assertThat(couponQueueRepository.getIssuedCount("scenario-coupon")).isEqualTo(successLimit);
        log.info("===== 선착순 이벤트가 종료되었습니다. =====");
    }
}
