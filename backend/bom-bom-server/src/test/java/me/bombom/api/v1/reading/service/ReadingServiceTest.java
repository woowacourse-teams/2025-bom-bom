package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class ReadingServiceTest {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @Autowired
    private MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Autowired
    private YearlyReadingRepository yearlyReadingRepository;

    private Member member;
    private TodayReading todayReading;
    private ContinueReading continueReading;
    private WeeklyReading weeklyReading;
    private MonthlyReadingSnapshot monthlyReadingSnapshot;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        yearlyReadingRepository.deleteAll();
        monthlyReadingSnapshotRepository.deleteAll();
        weeklyReadingRepository.deleteAll();
        todayReadingRepository.deleteAll();
        continueReadingRepository.deleteAll();
        memberRepository.deleteAll();

        String nickname = ("test_nickname_" + UUID.randomUUID()).substring(0, 20);
        String providerId = ("test_providerId_" + UUID.randomUUID()).substring(0, 20);

        member = memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
        todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        continueReading = continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        weeklyReading = weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));
        monthlyReadingSnapshot = monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingFixture(member));
        monthlyReadingRealtimeRepository.save(MonthlyReadingRealtime.builder()
                .memberId(member.getId())
                .currentCount(0)
                .build());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void 오늘_도착한_아티클을_읽으면_오늘_및_주간_읽기_횟수가_증가한다() {
        int initialTodayCount = todayReading.getCurrentCount();
        int initialWeeklyCount = weeklyReading.getCurrentCount();

        readingService.updateReadingCount(member.getId(), true);

        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedTodayReading.getCurrentCount()).isEqualTo(initialTodayCount + 1);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isEqualTo(initialWeeklyCount + 1);
        });
    }

    @Test
    void 오늘_도착한_아티클을_최초로_읽을_때_연속_읽기_횟수가_증가한다() {
        int initialContinueCount = continueReading.getDayCount();

        readingService.updateReadingCount(member.getId(), true);

        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 이미_연속_읽기_횟수가_증가하면_그날은_더이상_증가하지_않는다() {
        int initialContinueCount = continueReading.getDayCount();

        readingService.updateReadingCount(member.getId(), true);
        readingService.updateReadingCount(member.getId(), true);

        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 오늘_도착하지_않은_아티클을_읽으면_읽기_횟수가_증가하지_않는다() {
        int initialTodayCount = todayReading.getCurrentCount();
        int initialContinueCount = continueReading.getDayCount();
        int initialWeeklyCount = weeklyReading.getCurrentCount();

        readingService.updateReadingCount(member.getId(), false);

        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        ContinueReading updatedContinueReading = continueReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedTodayReading.getCurrentCount()).isEqualTo(initialTodayCount);
            softly.assertThat(updatedContinueReading.getDayCount()).isEqualTo(initialContinueCount);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isEqualTo(initialWeeklyCount);
        });
    }

    @Test
    void 저장된_rank를_사용해_상위_N명의_랭킹을_조회할_수_있다() {
        // given: 기본 멤버는 currentCount 10
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_r2", "pid_r2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_r3", "pid_r3"));

        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member2.getId())
                .currentCount(30)
                .build());
        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member3.getId())
                .currentCount(20)
                .build());

        // when: 순위 저장 배치 실행 후, 저장된 순위 기반 조회
        readingService.updateMonthlyRanking();
        int limit = 2;
        List<MonthlyReadingRankResponse> result = readingService.getMonthlyReadingRank(limit);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.size()).isEqualTo(limit);
            softly.assertThat(result.get(0).rank()).isLessThanOrEqualTo(result.get(1).rank());
            softly.assertThat(result.get(0).monthlyReadCount())
                    .isGreaterThanOrEqualTo(result.get(1).monthlyReadCount());
        });
    }

    @Test
    @Disabled
    void 나의_월간_순위와_전체_참여자_수를_조회할_수_있다() {
        // given: 기본 멤버는 currentCount 10
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr2", "pid_mr2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr3", "pid_mr3"));
        Member member4 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr4", "pid_mr4"));

        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member2.getId())
                .currentCount(30)
                .build());
        MonthlyReadingSnapshot member3Reading = monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member3.getId())
                .currentCount(20)
                .build());
        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member4.getId())
                .currentCount(20)
                .build());

        // when: 순위 반영 후 내 순위를 조회
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse memberRank = readingService.getMemberMonthlyReadingRank(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(memberRank.rank()).isGreaterThan(0L);
            softly.assertThat(memberRank.readCount()).isEqualTo(monthlyReadingSnapshot.getCurrentCount());
            softly.assertThat(memberRank.nextRankDifference())
                    .isEqualTo(member3Reading.getCurrentCount() - monthlyReadingSnapshot.getCurrentCount());
        });
    }

    @Test
    void 일등일_경우_앞_사람과의_차이는_0이다() {
        // given: 기본 멤버는 currentCount 10
        Member first = memberRepository.save(TestFixture.createUniqueMember("nickname_mr2", "pid_mr2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr3", "pid_mr3"));

        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(first.getId())
                .currentCount(30)
                .build());
        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member3.getId())
                .currentCount(20)
                .build());

        // when: 순위 반영 후 내 순위를 조회
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse memberRank = readingService.getMemberMonthlyReadingRank(first);

        // then
        assertThat(memberRank.nextRankDifference()).isEqualTo(0);
    }

    @Test
    void currentCount가_같으면_순위가_같다() {
        // given: 기본 멤버는 currentCount 10
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr2", "pid_mr2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr3", "pid_mr3"));

        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member2.getId())
                .currentCount(20)
                .build());
        monthlyReadingSnapshotRepository.save(MonthlyReadingSnapshot.builder()
                .memberId(member3.getId())
                .currentCount(20)
                .build());

        // when: 순위 반영 후 내 순위를 조회
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse memberRank2 = readingService.getMemberMonthlyReadingRank(member2);
        MemberMonthlyReadingRankResponse memberRank3 = readingService.getMemberMonthlyReadingRank(member3);

        // then
        assertThat(memberRank2.rank()).isEqualTo(memberRank3.rank());
    }

    @Test
    void 매월_읽기_수를_연간_읽기_수에_반영하고_월간은_초기화한다() {
        // given
        int monthlyCountBefore = monthlyReadingSnapshotRepository.findByMemberId(member.getId()).get().getCurrentCount();

        // when
        readingService.migrateMonthlyCountToYearlyAndReset();

        // then
        MonthlyReadingSnapshot monthlyReadingSnapshot = monthlyReadingSnapshotRepository.findByMemberId(member.getId()).get();
        YearlyReading yearlyReading = yearlyReadingRepository.findByMemberIdAndReadingYear(member.getId(),
                LocalDate.now().minusMonths(1).getYear()).get();

        assertSoftly(softly -> {
            softly.assertThat(yearlyReading.getCurrentCount()).isEqualTo(monthlyCountBefore);
            softly.assertThat(monthlyReadingSnapshot.getCurrentCount()).isEqualTo(0);
        });
    }
}
