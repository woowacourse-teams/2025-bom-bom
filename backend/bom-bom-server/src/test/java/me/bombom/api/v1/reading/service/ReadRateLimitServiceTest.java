package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.MemberReadTokenBucket;
import me.bombom.api.v1.reading.repository.MemberReadTokenBucketRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReadRateLimitServiceTest {

    @Autowired
    private ReadRateLimitService readRateLimitService;

    @Autowired
    private MemberReadTokenBucketRepository memberReadTokenBucketRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        memberReadTokenBucketRepository.deleteAllInBatch();
        monthlyReadingRealtimeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.createMemberFixture("test@test.com", "testUser"));
        monthlyReadingRealtimeRepository.save(TestFixture.monthlyReadingRealtimeFixture(member, 0));
    }

    @Test
    void 버킷이_없는_경우_최초_생성_후_토큰_차감() {
        // given
        assertThat(memberReadTokenBucketRepository.findById(member.getId())).isEmpty();

        // when
        boolean result = readRateLimitService.tryConsumeReadCountToken(member.getId(), LocalDateTime.now());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            softly.assertThat(memberReadTokenBucketRepository.findById(member.getId()))
                    .isPresent()
                    .hasValueSatisfying(bucket -> softly.assertThat(bucket.getTokens()).isLessThan(3.0));
        });
    }

    @Test
    void 토큰이_남아있으면_차감_후_허용() {
        // given
        memberReadTokenBucketRepository.save(TestFixture.createMemberReadTokenBucket(member.getId(), 3.0));

        // when
        boolean result = readRateLimitService.tryConsumeReadCountToken(member.getId(), LocalDateTime.now());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            softly.assertThat(memberReadTokenBucketRepository.findById(member.getId()))
                    .hasValueSatisfying(bucket -> softly.assertThat(bucket.getTokens()).isLessThan(3.0));
        });
    }

    @Test
    void 토큰_소진_시_차단() {
        // given
        memberReadTokenBucketRepository.save(TestFixture.createMemberReadTokenBucket(member.getId(), 0.5));

        // when
        boolean result = readRateLimitService.tryConsumeReadCountToken(member.getId(), LocalDateTime.now());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isFalse();
            softly.assertThat(memberReadTokenBucketRepository.findById(member.getId()))
                    .hasValueSatisfying(bucket -> softly.assertThat(bucket.getTokens()).isLessThan(1.0));
        });
    }

    @Test
    void refillSeconds_경과_후_토큰_충전되어_재허용() {
        // given
        LocalDateTime savedAt = LocalDateTime.now();
        memberReadTokenBucketRepository.save(TestFixture.createMemberReadTokenBucket(member.getId(), 0.5));

        // when - 55초 뒤
        boolean result = readRateLimitService.tryConsumeReadCountToken(member.getId(), savedAt.plusSeconds(55));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 소수점_토큰_이월되어_짧은_대기_후_재허용() {
        // given
        LocalDateTime savedAt = LocalDateTime.now().withNano(0);
        memberReadTokenBucketRepository.save(TestFixture.createMemberReadTokenBucket(member.getId(), 0.0, savedAt));

        // when - 80초 뒤 소비 (80/50 = 1.6 충전 → 1.6 - 1 = 0.6 이월)
        boolean firstResult = readRateLimitService.tryConsumeReadCountToken(member.getId(), savedAt.plusSeconds(80));

        // then - 0.6 이월 저장 확인
        double carryOver = memberReadTokenBucketRepository.findById(member.getId())
                .map(MemberReadTokenBucket::getTokens)
                .orElseThrow();
        System.out.println("1차 소비 후 이월 토큰: " + carryOver);

        assertSoftly(softly -> {
            softly.assertThat(firstResult).isTrue();
            softly.assertThat(carryOver).isGreaterThan(0.0).isLessThan(1.0);
        });

        // when - 30초 뒤 재소비 (이월 0.6 + 30/50=0.6 → 합계 1.2)
        boolean secondResult = readRateLimitService.tryConsumeReadCountToken(member.getId(), savedAt.plusSeconds(110));

        // then - 이월 덕분에 50초를 안 채워도 허용
        assertThat(secondResult).isTrue();
    }
}
