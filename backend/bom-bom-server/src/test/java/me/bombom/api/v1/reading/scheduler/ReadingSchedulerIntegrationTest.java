package me.bombom.api.v1.reading.scheduler;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReadingSchedulerIntegrationTest {

    @Autowired
    private ReadingScheduler readingScheduler;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Autowired
    private ContinueReadingSnapshotRepository continueReadingSnapshotRepository;

    @Autowired
    private MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Autowired
    private YearlyReadingRepository yearlyReadingRepository;

    @Autowired
    private MutableClock clock;

    @Test
    void 일간_초기화_스케줄러는_오늘_읽기_카운트를_초기화한다() {
        // given
        clock.setDate(LocalDate.of(2026, 4, 26));
        Member member = memberRepository.save(TestFixture.createUniqueMember("일간초기화회원", "daily-reset"));
        TodayReading todayReading = todayReadingRepository.save(TestFixture.todayReadingFixture(member));

        // when
        readingScheduler.dailyResetReadingCount();

        // then
        TodayReading updatedReading = todayReadingRepository.findById(todayReading.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedReading.getTotalCount()).isZero();
            softly.assertThat(updatedReading.getCurrentCount()).isZero();
            softly.assertThat(updatedReading.getReadCount()).isZero();
        });
    }

    @Test
    void 주간_초기화_스케줄러는_주간_현재_읽기수를_초기화한다() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("주간초기화회원", "weekly-reset"));
        WeeklyReading weeklyReading = weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));

        // when
        readingScheduler.weeklyResetReadingCount();

        // then
        WeeklyReading updatedReading = weeklyReadingRepository.findById(weeklyReading.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updatedReading.getCurrentCount()).isZero();
            softly.assertThat(updatedReading.getGoalCount()).isEqualTo(5);
        });
    }

    @Test
    void 연속읽기_랭킹_스냅샷_스케줄러는_실시간_연속일로_순위를_갱신한다() {
        // given
        Member first = memberRepository.save(TestFixture.createUniqueMember("연속일등회원", "continue-first"));
        Member second = memberRepository.save(TestFixture.createUniqueMember("연속이등회원", "continue-second"));
        continueReadingRepository.save(ContinueReadingRealtime.builder()
                .memberId(first.getId())
                .dayCount(10)
                .build());
        continueReadingRepository.save(ContinueReadingRealtime.builder()
                .memberId(second.getId())
                .dayCount(5)
                .build());

        // when
        readingScheduler.tenMinutelyCalculateContinueReadingRankingSnapshot();

        // then
        List<ContinueReadingSnapshot> snapshots = continueReadingSnapshotRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ContinueReadingSnapshot::getRankOrder))
                .toList();
        assertSoftly(softly -> {
            softly.assertThat(snapshots).hasSize(2);
            softly.assertThat(snapshots.getFirst().getMemberId()).isEqualTo(first.getId());
            softly.assertThat(snapshots.getFirst().getDayCount()).isEqualTo(10);
            softly.assertThat(snapshots.getFirst().getRankOrder()).isEqualTo(1L);
            softly.assertThat(snapshots.getLast().getMemberId()).isEqualTo(second.getId());
            softly.assertThat(snapshots.getLast().getDayCount()).isEqualTo(5);
            softly.assertThat(snapshots.getLast().getRankOrder()).isEqualTo(2L);
        });
    }

    @Test
    void 월간_초기화_스케줄러는_월간_읽기수를_연간에_반영하고_월간값을_초기화한다() {
        // given
        clock.setDate(LocalDate.of(2026, 1, 1));
        Member member = memberRepository.save(TestFixture.createUniqueMember("월간초기화회원", "monthly-reset"));
        MonthlyReadingSnapshot snapshot = monthlyReadingSnapshotRepository.save(
                TestFixture.monthlyReadingSnapshotWithRank(member, 7, 1, 0)
        );
        MonthlyReadingRealtime realtime = monthlyReadingRealtimeRepository.save(
                TestFixture.monthlyReadingRealtimeFixture(member, 7)
        );

        // when
        readingScheduler.monthlyResetReadingCount();

        // then
        MonthlyReadingSnapshot updatedSnapshot = monthlyReadingSnapshotRepository.findById(snapshot.getId()).orElseThrow();
        MonthlyReadingRealtime updatedRealtime = monthlyReadingRealtimeRepository.findById(realtime.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(yearlyReadingRepository.findByMemberIdAndReadingYear(member.getId(), 2025))
                    .get()
                    .extracting("currentCount")
                    .isEqualTo(7);
            softly.assertThat(updatedSnapshot.getCurrentCount()).isZero();
            softly.assertThat(updatedRealtime.getCurrentCount()).isZero();
        });
    }
}
