package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReadingServiceLifecycleTest {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Autowired
    private ContinueReadingSnapshotRepository continueReadingSnapshotRepository;

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

    @Autowired
    private ContinueReadingShieldRepository continueReadingShieldRepository;

    @Test
    void 회원_읽기_정보를_모두_삭제한다() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("읽기삭제회원", "reading-delete"));
        continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        continueReadingSnapshotRepository.save(ContinueReadingSnapshot.create(member.getId(), 10, 1L));
        todayReadingRepository.save(TestFixture.todayReadingFixture(member));
        weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));
        MonthlyReadingSnapshot monthlySnapshot = monthlyReadingSnapshotRepository.save(
                TestFixture.monthlyReadingSnapshotWithRank(member, 7, 1, 0)
        );
        MonthlyReadingRealtime monthlyRealtime = monthlyReadingRealtimeRepository.save(
                MonthlyReadingRealtime.builder()
                        .memberId(member.getId())
                        .currentCount(7)
                        .build()
        );
        yearlyReadingRepository.save(YearlyReading.builder()
                .memberId(member.getId())
                .readingYear(2026)
                .currentCount(7)
                .build());
        continueReadingShieldRepository.save(ContinueReadingShield.create(member.getId()));

        // when
        readingService.deleteAllByMemberId(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(continueReadingRepository.findByMemberId(member.getId())).isEmpty();
            softly.assertThat(continueReadingSnapshotRepository.findAll())
                    .noneMatch(snapshot -> snapshot.getMemberId().equals(member.getId()));
            softly.assertThat(todayReadingRepository.findByMemberId(member.getId())).isEmpty();
            softly.assertThat(weeklyReadingRepository.findByMemberId(member.getId())).isEmpty();
            softly.assertThat(monthlyReadingSnapshotRepository.findById(monthlySnapshot.getId())).isEmpty();
            softly.assertThat(monthlyReadingRealtimeRepository.findById(monthlyRealtime.getId())).isEmpty();
            softly.assertThat(yearlyReadingRepository.findByMemberIdAndReadingYear(member.getId(), 2026)).isEmpty();
            softly.assertThat(continueReadingShieldRepository.findByMemberId(member.getId())).isEmpty();
        });
    }

    @Test
    void 주간_목표_수정_시_회원이_없으면_에러() {
        // when & then
        assertThatThrownBy(() -> readingService.updateWeeklyGoalCount(-1L, 5))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 주간_목표_수정_시_주간_읽기정보가_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("주간없음회원", "weekly-missing"));

        // when & then
        assertThatThrownBy(() -> readingService.updateWeeklyGoalCount(member.getId(), 5))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_정보_조회_시_연속읽기정보가_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("연속없음회원", "info-continue-missing"));

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_정보_조회_시_오늘_읽기정보가_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("오늘없음회원", "info-today-missing"));
        continueReadingRepository.save(TestFixture.continueReadingFixture(member));

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_정보_조회_시_주간_읽기정보가_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("주간정보없음회원", "info-weekly-missing"));
        continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        todayReadingRepository.save(TestFixture.todayReadingFixture(member));

        // when & then
        assertThatThrownBy(() -> readingService.getReadingInformation(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 월간_읽기수_조회_시_월간_실시간정보가_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("월간없음회원", "monthly-count-missing"));

        // when & then
        assertThatThrownBy(() -> readingService.getMemberMonthlyReadingCount(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 내_월간_랭킹_조회_시_스냅샷이_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("월간랭킹없음회원", "monthly-rank-missing"));

        // when & then
        assertThatThrownBy(() -> readingService.getMemberMonthlyReadingRank(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 내_연속_랭킹_조회_시_스냅샷이_없으면_에러() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("연속랭킹없음회원", "continue-rank-missing"));

        // when & then
        assertThatThrownBy(() -> readingService.getMemberContinueReadingRank(member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 가이드메일_읽기_반영은_등록일이면_일간_주간_연속읽기와_월간읽기를_증가시킨다() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("가이드등록일회원", "guide-register-day"));
        TodayReading todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        WeeklyReading weeklyReading = weeklyReadingRepository.save(WeeklyReading.create(member.getId()));
        ContinueReadingRealtime continueReading =
                continueReadingRepository.save(ContinueReadingRealtime.create(member.getId()));
        MonthlyReadingRealtime monthlyReading =
                monthlyReadingRealtimeRepository.save(MonthlyReadingRealtime.create(member.getId()));

        // when
        readingService.updateReadingCountForGuideMail(member.getId(), true);

        // then
        assertSoftly(softly -> {
            softly.assertThat(todayReadingRepository.findById(todayReading.getId()).orElseThrow().getCurrentCount())
                    .isEqualTo(1);
            softly.assertThat(todayReadingRepository.findById(todayReading.getId()).orElseThrow().getReadCount())
                    .isEqualTo(1);
            softly.assertThat(weeklyReadingRepository.findById(weeklyReading.getId()).orElseThrow().getCurrentCount())
                    .isEqualTo(1);
            softly.assertThat(continueReadingRepository.findById(continueReading.getId()).orElseThrow().getDayCount())
                    .isEqualTo(1);
            softly.assertThat(monthlyReadingRealtimeRepository.findById(monthlyReading.getId())
                            .orElseThrow()
                            .getCurrentCount())
                    .isEqualTo(1);
        });
    }

    @Test
    void 가이드메일_읽기_반영은_등록일이_아니면_월간읽기만_증가시킨다() {
        // given
        Member member = memberRepository.save(TestFixture.createUniqueMember("가이드비등록일회원", "guide-not-register-day"));
        TodayReading todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        WeeklyReading weeklyReading = weeklyReadingRepository.save(WeeklyReading.create(member.getId()));
        ContinueReadingRealtime continueReading =
                continueReadingRepository.save(ContinueReadingRealtime.create(member.getId()));
        MonthlyReadingRealtime monthlyReading =
                monthlyReadingRealtimeRepository.save(MonthlyReadingRealtime.create(member.getId()));

        // when
        readingService.updateReadingCountForGuideMail(member.getId(), false);

        // then
        assertSoftly(softly -> {
            softly.assertThat(todayReadingRepository.findById(todayReading.getId()).orElseThrow().getCurrentCount())
                    .isZero();
            softly.assertThat(todayReadingRepository.findById(todayReading.getId()).orElseThrow().getReadCount())
                    .isZero();
            softly.assertThat(weeklyReadingRepository.findById(weeklyReading.getId()).orElseThrow().getCurrentCount())
                    .isZero();
            softly.assertThat(continueReadingRepository.findById(continueReading.getId()).orElseThrow().getDayCount())
                    .isZero();
            softly.assertThat(monthlyReadingRealtimeRepository.findById(monthlyReading.getId())
                            .orElseThrow()
                            .getCurrentCount())
                    .isEqualTo(1);
        });
    }

    @Test
    void 회원_읽기_초기화는_0점_최하위와_공동_순위로_등록한다() {
        // given
        Member existingMember = memberRepository.save(TestFixture.createUniqueMember("기존0점회원", "init-zero-existing"));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(existingMember, 0, 3, 0));
        continueReadingSnapshotRepository.save(ContinueReadingSnapshot.create(existingMember.getId(), 0, 2L));
        Member newMember = memberRepository.save(TestFixture.createUniqueMember("신규0점회원", "init-zero-new"));

        // when
        readingService.initializeReadingInformation(newMember.getId());

        // then
        MonthlyReadingSnapshot monthlySnapshot =
                monthlyReadingSnapshotRepository.findByMemberId(newMember.getId()).orElseThrow();
        ContinueReadingSnapshot continueSnapshot = findContinueSnapshot(newMember.getId());
        assertSoftly(softly -> {
            softly.assertThat(monthlySnapshot.getRankOrder()).isEqualTo(3L);
            softly.assertThat(monthlySnapshot.getNextRankDifference()).isZero();
            softly.assertThat(continueSnapshot.getRankOrder()).isEqualTo(2L);
            softly.assertThat(continueSnapshot.getDayCount()).isZero();
            softly.assertThat(todayReadingRepository.findByMemberId(newMember.getId())).isPresent();
            softly.assertThat(weeklyReadingRepository.findByMemberId(newMember.getId())).isPresent();
            softly.assertThat(monthlyReadingRealtimeRepository.findByMemberId(newMember.getId())).isPresent();
            softly.assertThat(continueReadingShieldRepository.findByMemberId(newMember.getId())).isPresent();
        });
    }

    @Test
    void 회원_읽기_초기화는_연속랭킹_스냅샷이_없으면_1위로_등록한다() {
        // given
        Member existingMember =
                memberRepository.save(TestFixture.createUniqueMember("월간기존회원", "init-no-continue-existing"));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(existingMember, 0, 3, 0));
        Member newMember = memberRepository.save(TestFixture.createUniqueMember("연속랭킹첫회원", "init-no-continue-new"));

        // when
        readingService.initializeReadingInformation(newMember.getId());

        // then
        ContinueReadingSnapshot continueSnapshot = findContinueSnapshot(newMember.getId());
        assertSoftly(softly -> {
            softly.assertThat(continueSnapshot.getRankOrder()).isEqualTo(1L);
            softly.assertThat(continueSnapshot.getDayCount()).isZero();
        });
    }

    @Test
    void 회원_읽기_초기화는_읽기수가_있는_최하위의_다음_순위로_등록한다() {
        // given
        Member existingMember = memberRepository.save(TestFixture.createUniqueMember("기존읽기회원", "init-read-existing"));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(existingMember, 5, 3, 2));
        continueReadingSnapshotRepository.save(ContinueReadingSnapshot.create(existingMember.getId(), 4, 2L));
        Member newMember = memberRepository.save(TestFixture.createUniqueMember("신규읽기회원", "init-read-new"));

        // when
        readingService.initializeReadingInformation(newMember.getId());

        // then
        MonthlyReadingSnapshot monthlySnapshot =
                monthlyReadingSnapshotRepository.findByMemberId(newMember.getId()).orElseThrow();
        ContinueReadingSnapshot continueSnapshot = findContinueSnapshot(newMember.getId());
        YearlyReading yearlyReading = yearlyReadingRepository.findAll().stream()
                .filter(reading -> reading.getMemberId().equals(newMember.getId()))
                .findFirst()
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(monthlySnapshot.getRankOrder()).isEqualTo(4L);
            softly.assertThat(monthlySnapshot.getNextRankDifference()).isEqualTo(5L);
            softly.assertThat(continueSnapshot.getRankOrder()).isEqualTo(3L);
            softly.assertThat(yearlyReading.getCurrentCount()).isZero();
        });
    }

    private ContinueReadingSnapshot findContinueSnapshot(Long memberId) {
        return continueReadingSnapshotRepository.findAll()
                .stream()
                .filter(snapshot -> snapshot.getMemberId().equals(memberId))
                .findFirst()
                .orElseThrow();
    }
}
