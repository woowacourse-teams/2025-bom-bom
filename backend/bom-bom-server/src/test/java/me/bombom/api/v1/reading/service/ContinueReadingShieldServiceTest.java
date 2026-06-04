package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryType;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@IntegrationTest
@Import(ContinueReadingShieldServiceTest.FixedClockConfig.class)
class ContinueReadingShieldServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 5, 1);
    private static final LocalDate MONTH_START_DATE = LocalDate.of(2026, 5, 1);

    @Autowired
    private ContinueReadingShieldService continueReadingShieldService;

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRealtimeRepository;

    @Autowired
    private ContinueReadingShieldRepository continueReadingShieldRepository;

    @Autowired
    private ContinueReadingShieldHistoryRepository continueReadingShieldHistoryRepository;

    @BeforeEach
    void setUp() {
        continueReadingShieldHistoryRepository.deleteAllInBatch();
        continueReadingShieldRepository.deleteAllInBatch();
        continueReadingRealtimeRepository.deleteAllInBatch();
        todayReadingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 가입_초기화_시_보호막_1개와_월_지급_이력을_생성한다() {
        Member member = createMember();

        continueReadingShieldService.initializeShield(member.getId());

        ContinueReadingShield shield = continueReadingShieldRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(shield.getRemainingCount()).isEqualTo(1);
            softly.assertThat(continueReadingShieldHistoryRepository.countByMemberIdAndTypeAndEventDate(
                    member.getId(),
                    ContinueReadingShieldHistoryType.GRANT,
                    MONTH_START_DATE
            )).isEqualTo(1L);
        });
    }

    @Test
    void 보호막은_같은_날_한_번만_사용된다() {
        Member member = createMember();
        continueReadingShieldRepository.save(ContinueReadingShield.create(member.getId()));
        LocalDate targetDate = LocalDate.of(2026, 4, 30);

        boolean firstResult = continueReadingShieldService.useShield(member.getId(), targetDate);
        boolean secondResult = continueReadingShieldService.useShield(member.getId(), targetDate);

        ContinueReadingShield shield = continueReadingShieldRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(firstResult).isTrue();
            softly.assertThat(secondResult).isFalse();
            softly.assertThat(shield.getRemainingCount()).isZero();
            softly.assertThat(continueReadingShieldHistoryRepository.countByMemberIdAndTypeAndEventDate(
                    member.getId(),
                    ContinueReadingShieldHistoryType.USE,
                    targetDate
            )).isEqualTo(1L);
        });
    }

    @Test
    void 월초_보호막_리셋은_같은_월에_한_번만_적용된다() {
        Member member = createMember();
        continueReadingShieldRepository.save(ContinueReadingShield.builder()
                .memberId(member.getId())
                .remainingCount(0)
                .build());

        continueReadingShieldService.resetMonthlyShieldsIfFirstDay();

        ContinueReadingShield grantedShield = continueReadingShieldRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(grantedShield.getRemainingCount()).isEqualTo(1);

        continueReadingShieldService.useShield(member.getId(), LocalDate.of(2026, 5, 1));
        continueReadingShieldService.resetMonthlyShieldsIfFirstDay();

        ContinueReadingShield usedShield = continueReadingShieldRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(usedShield.getRemainingCount()).isZero();
            softly.assertThat(continueReadingShieldHistoryRepository.countByMemberIdAndTypeAndEventDate(
                    member.getId(),
                    ContinueReadingShieldHistoryType.GRANT,
                    MONTH_START_DATE
            )).isEqualTo(1L);
        });
    }

    @Test
    void 월경계에서는_전날_미독을_보호막으로_처리한_뒤_이번달_보호막을_1개로_리셋한다() {
        Member member = createMember();
        LocalDate targetDate = LocalDate.of(2026, 4, 30);
        todayReadingRepository.save(TodayReading.builder()
                .memberId(member.getId())
                .totalCount(3)
                .currentCount(0)
                .readCount(0)
                .build());
        continueReadingRealtimeRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(10)
                .maxDayCount(15)
                .build());
        continueReadingShieldRepository.save(ContinueReadingShield.create(member.getId()));

        readingService.resetContinueReadingCount();
        continueReadingShieldService.resetMonthlyShieldsIfFirstDay();

        ContinueReadingRealtime continueReading = continueReadingRealtimeRepository.findByMemberId(member.getId()).orElseThrow();
        ContinueReadingShield shield = continueReadingShieldRepository.findByMemberId(member.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(continueReading.getDayCount()).isEqualTo(10);
            softly.assertThat(continueReading.getMaxDayCount()).isEqualTo(15);
            softly.assertThat(shield.getRemainingCount()).isEqualTo(1);
            softly.assertThat(continueReadingShieldHistoryRepository.countByMemberIdAndTypeAndEventDate(
                    member.getId(),
                    ContinueReadingShieldHistoryType.USE,
                    targetDate
            )).isEqualTo(1L);
            softly.assertThat(continueReadingShieldHistoryRepository.countByMemberIdAndTypeAndEventDate(
                    member.getId(),
                    ContinueReadingShieldHistoryType.GRANT,
                    MONTH_START_DATE
            )).isEqualTo(1L);
        });
    }

    private Member createMember() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return memberRepository.save(TestFixture.createUniqueMember("shield_" + suffix, "shield_pid_" + suffix));
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(FIXED_DATE.atStartOfDay(SEOUL_ZONE).toInstant(), SEOUL_ZONE);
        }
    }
}
