package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.badge.domain.Badge;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.domain.ChallengeBadge;
import me.bombom.api.v1.badge.domain.RankingBadge;
import me.bombom.api.v1.badge.repository.BadgeRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshotMeta;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.ContinueReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.MemberContinueReadingRankResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotMetaRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReadingServiceTest {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Autowired
    private ContinueReadingSnapshotRepository continueReadingRankingSnapshotRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private WeeklyReadingRepository weeklyReadingRepository;

    @Autowired
    private MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    @Autowired
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Autowired
    private MonthlyReadingSnapshotMetaRepository monthlyReadingSnapshotMetaRepository;

    @Autowired
    private YearlyReadingRepository yearlyReadingRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    private Member member;
    private TodayReading todayReading;
    private ContinueReadingRealtime continueReading;
    private WeeklyReading weeklyReading;
    private MonthlyReadingSnapshot monthlyReadingSnapshot;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        badgeRepository.deleteAllInBatch();
        yearlyReadingRepository.deleteAllInBatch();
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        weeklyReadingRepository.deleteAllInBatch();
        todayReadingRepository.deleteAllInBatch();
        continueReadingRepository.deleteAllInBatch();
        continueReadingRankingSnapshotRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        assertThat(continueReadingRankingSnapshotRepository.count()).isZero();

        String nickname = ("test_nickname_" + UUID.randomUUID()).substring(0, 20);
        String providerId = ("test_providerId_" + UUID.randomUUID()).substring(0, 20);

        member = memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
        todayReading = todayReadingRepository.save(TestFixture.todayReadingFixtureZeroCurrentCount(member));
        continueReading = continueReadingRepository.save(TestFixture.continueReadingFixture(member));
        continueReadingRankingSnapshotRepository.save(
                ContinueReadingSnapshot.create(member.getId(), continueReading.getDayCount(), 1L)
        );
        weeklyReading = weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(member));
        monthlyReadingSnapshot = monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingFixture(member));
        monthlyReadingRealtimeRepository.save(TestFixture.monthlyReadingRealtimeFixture(member, 0));

        memberRepository.flush();
        todayReadingRepository.flush();
        continueReadingRepository.flush();
        continueReadingRankingSnapshotRepository.flush();
        weeklyReadingRepository.flush();
        monthlyReadingSnapshotRepository.flush();
        monthlyReadingRealtimeRepository.flush();
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

        ContinueReadingRealtime updatedContinueReadingRealtime = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReadingRealtime.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 이미_연속_읽기_횟수가_증가하면_그날은_더이상_증가하지_않는다() {
        int initialContinueCount = continueReading.getDayCount();

        readingService.updateReadingCount(member.getId(), true);
        readingService.updateReadingCount(member.getId(), true);

        ContinueReadingRealtime updatedContinueReadingRealtime = continueReadingRepository.findByMemberId(member.getId()).get();

        assertThat(updatedContinueReadingRealtime.getDayCount()).isEqualTo(initialContinueCount + 1);
    }

    @Test
    void 오늘_도착하지_않은_아티클을_읽으면_읽기_횟수가_증가하지_않는다() {
        int initialTodayCount = todayReading.getCurrentCount();
        int initialContinueCount = continueReading.getDayCount();
        int initialWeeklyCount = weeklyReading.getCurrentCount();

        readingService.updateReadingCount(member.getId(), false);

        TodayReading updatedTodayReading = todayReadingRepository.findByMemberId(member.getId()).get();
        ContinueReadingRealtime updatedContinueReadingRealtime = continueReadingRepository.findByMemberId(member.getId()).get();
        WeeklyReading updatedWeeklyReading = weeklyReadingRepository.findByMemberId(member.getId()).get();

        assertSoftly(softly -> {
            softly.assertThat(updatedTodayReading.getCurrentCount()).isEqualTo(initialTodayCount);
            softly.assertThat(updatedContinueReadingRealtime.getDayCount()).isEqualTo(initialContinueCount);
            softly.assertThat(updatedWeeklyReading.getCurrentCount()).isEqualTo(initialWeeklyCount);
        });
    }

    @Test
    void 저장된_rank를_사용해_상위_N명의_랭킹을_조회할_수_있다() {
        // given: 기본 멤버는 currentCount 10
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_r2", "pid_r2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_r3", "pid_r3"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member2, 30));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member3, 20));

        // when: 순위 저장 배치 실행 후, 저장된 순위 기반 조회
        readingService.updateMonthlyRanking();
        int limit = 2;
        MonthlyReadingRankingResponse result = readingService.getMonthlyReadingRank(limit);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.data().size()).isEqualTo(limit);
            softly.assertThat(result.data().get(0).rank()).isLessThanOrEqualTo(result.data().get(1).rank());
            softly.assertThat(result.data().get(0).monthlyReadCount())
                    .isGreaterThanOrEqualTo(result.data().get(1).monthlyReadCount());
        });
    }

    @Test
    void 연속_읽기_일수_기준으로_상위_N명의_랭킹을_조회할_수_있다() {
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_st2", "pid_st2"));
        continueReadingRepository.save(
                ContinueReadingRealtime.builder()
                        .memberId(member2.getId())
                        .dayCount(30)
                        .build()
        );

        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_st3", "pid_st3"));
        continueReadingRepository.save(
                ContinueReadingRealtime.builder()
                        .memberId(member3.getId())
                        .dayCount(20)
                        .build()
        );

        readingService.updateContinueReadingRankingSnapshot();

        int limit = 2;
        ContinueReadingRankingResponse result = readingService.getContinueReadingRank(limit);

        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(limit);
            softly.assertThat(result.data().get(0).dayCount()).isEqualTo(30);
            softly.assertThat(result.data().get(1).dayCount()).isEqualTo(20);
            softly.assertThat(result.data().get(0).rank()).isEqualTo(1L);
            softly.assertThat(result.data().get(1).rank()).isEqualTo(2L);
        });
    }

    @Test
    void 가입_시_추가된_연속_읽기_스냅샷으로_신규_회원이_랭킹_최하위_공동_순위에_포함된다() {
        // given: 기존 회원 스냅샷이 있는 상태
        ContinueReadingRankingResponse initial = readingService.getContinueReadingRank(10);
        assertThat(initial.data()).hasSize(1);
        assertThat(initial.data().get(0).rank()).isEqualTo(1L);
        assertThat(initial.data().get(0).dayCount()).isEqualTo(10);

        // when: 가입 시 continue_reading(0) 및 스냅샷 row가 함께 생성됨 (월간과 동일 패턴)
        Member newMember = memberRepository.save(TestFixture.createUniqueMember("nickname_st_new", "pid_st_new"));
        readingService.initializeReadingInformation(newMember.getId());

        // then: 신규 사용자는 dayCount=0의 최하위 공동 순위로 포함된다
        ContinueReadingRankingResponse result = readingService.getContinueReadingRank(10);

        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(2);
            softly.assertThat(result.data().get(0).nickname()).isEqualTo(member.getNickname());
            softly.assertThat(result.data().get(0).rank()).isEqualTo(1L);
            softly.assertThat(result.data().get(0).dayCount()).isEqualTo(10);

            softly.assertThat(result.data().get(1).nickname()).isEqualTo(newMember.getNickname());
            softly.assertThat(result.data().get(1).rank()).isEqualTo(2L);
            softly.assertThat(result.data().get(1).dayCount()).isEqualTo(0);
        });
    }

    @Test
    void 나의_연속_읽기_순위를_조회할_수_있다() {
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_st_me2", "pid_st_me2"));
        continueReadingRepository.save(
                ContinueReadingRealtime.builder()
                        .memberId(member2.getId())
                        .dayCount(30)
                        .build()
        );

        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_st_me3", "pid_st_me3"));
        continueReadingRepository.save(
                ContinueReadingRealtime.builder()
                        .memberId(member3.getId())
                        .dayCount(20)
                        .build()
        );

        readingService.updateContinueReadingRankingSnapshot();

        MemberContinueReadingRankResponse result = readingService.getMemberContinueReadingRank(member);

        assertSoftly(softly -> {
            softly.assertThat(result.rank()).isEqualTo(3L);
            softly.assertThat(result.dayCount()).isEqualTo(10);
            softly.assertThat(result.nickname()).isEqualTo(member.getNickname());
        });
    }

    @Test
    void 연속_읽기_일수가_0이어도_나의_순위는_최하위_공동_순위로_조회된다() {
        ContinueReadingRealtime cr = continueReadingRepository.findByMemberId(member.getId()).get();
        cr.resetDayCount();
        continueReadingRepository.save(cr);

        Member other = memberRepository.save(TestFixture.createUniqueMember("nickname_gt0", "pid_gt0"));
        continueReadingRepository.save(
                ContinueReadingRealtime.builder()
                        .memberId(other.getId())
                        .dayCount(5)
                        .build()
        );
        continueReadingRepository.flush();

        readingService.updateContinueReadingRankingSnapshot();

        MemberContinueReadingRankResponse result = readingService.getMemberContinueReadingRank(member);

        assertSoftly(softly -> {
            softly.assertThat(result.dayCount()).isEqualTo(0);
            softly.assertThat(result.rank()).isEqualTo(2L);
            softly.assertThat(result.nickname()).isEqualTo(member.getNickname());
        });
    }

    @Test
    void 랭킹_업데이트된_시간을_조회할_수_있다() {
        // given
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);;
        int limit = 2;

        // when
        monthlyReadingSnapshotMetaRepository.save(MonthlyReadingSnapshotMeta.builder()
                .id(1L)
                .snapshotAt(dateTime)
                .build());
        MonthlyReadingRankingResponse monthlyReadingRank = readingService.getMonthlyReadingRank(limit);

        // then
        assertThat(monthlyReadingRank.rankingUpdatedAt()).isEqualTo(dateTime);
    }

    @Test
    @Disabled
    void 나의_월간_순위와_전체_참여자_수를_조회할_수_있다() {
        // given: 기본 멤버는 currentCount 10
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr2", "pid_mr2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr3", "pid_mr3"));
        Member member4 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr4", "pid_mr4"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member2, 30));
        MonthlyReadingSnapshot member3Reading = monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member3, 20));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member4, 20));

        // when: 순위 반영 후 내 순위를 조회
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse memberRank = readingService.getMemberMonthlyReadingRank(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(memberRank.rank()).isGreaterThan(0L);
            softly.assertThat(memberRank.monthlyReadCount()).isEqualTo(monthlyReadingSnapshot.getCurrentCount());
            softly.assertThat(memberRank.nextRankDifference())
                    .isEqualTo(member3Reading.getCurrentCount() - monthlyReadingSnapshot.getCurrentCount());
        });
    }

    @Test
    void 일등일_경우_앞_사람과의_차이는_0이다() {
        // given: 기본 멤버는 currentCount 10
        Member first = memberRepository.save(TestFixture.createUniqueMember("nickname_mr2", "pid_mr2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("nickname_mr3", "pid_mr3"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(first, 30));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member3, 20));

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

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member2, 20));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshot(member3, 20));

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

    @Test
    void 매월_초기화_시_상위_3명에게_랭킹_뱃지를_발급한다() {
        // given
        // setUp()에서 생성된 snapshot 삭제 (rankOrder가 없어서 제외)
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("member2", "provider2"));
        Member member3 = memberRepository.save(TestFixture.createUniqueMember("member3", "provider3"));
        Member member4 = memberRepository.save(TestFixture.createUniqueMember("member4", "provider4"));

        // 랭킹 설정: snapshot에 rankOrder 설정
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member2, 30, 1, 0));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member3, 20, 2, 10));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member4, 10, 3, 10));

        // when
        readingService.migrateMonthlyCountToYearlyAndReset();

        // then
        List<Badge> badges = badgeRepository.findAll();
        assertThat(badges).hasSize(3);

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        RankingBadge goldBadge = findRankingBadge(badges, member2.getId(), BadgeGrade.GOLD);
        RankingBadge silverBadge = findRankingBadge(badges, member3.getId(), BadgeGrade.SILVER);
        RankingBadge bronzeBadge = findRankingBadge(badges, member4.getId(), BadgeGrade.BRONZE);

        assertSoftly(softly -> {
            softly.assertThat(goldBadge).isNotNull();
            softly.assertThat(silverBadge).isNotNull();
            softly.assertThat(bronzeBadge).isNotNull();
            
            softly.assertThat(goldBadge.getPeriodYear()).isEqualTo(lastMonth.getYear());
            softly.assertThat(goldBadge.getPeriodMonth()).isEqualTo(lastMonth.getMonthValue());
        });
    }

    @Test
    void 랭킹_대상이_없을_때는_뱃지를_발급하지_않는다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();

        // when
        readingService.migrateMonthlyCountToYearlyAndReset();

        // then
        assertThat(badgeRepository.count()).isZero();
    }

    @Test
    void 월간_랭킹_조회_시_이전달_랭킹_뱃지가_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        
        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        Member member2 = memberRepository.save(TestFixture.createUniqueMember("member2", "provider2"));
        
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member2, 20, 2, 10));
        
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        RankingBadge rankingBadge = RankingBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .periodYear(lastMonth.getYear())
                .periodMonth(lastMonth.getMonthValue())
                .build();
        badgeRepository.save(rankingBadge);
        
        // when
        MonthlyReadingRankingResponse result = readingService.getMonthlyReadingRank(10);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(2);
            softly.assertThat(result.data().get(0).badges()).isNotNull();
            softly.assertThat(result.data().get(0).badges().ranking()).isNotNull();
            softly.assertThat(result.data().get(0).badges().ranking().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.data().get(0).badges().ranking().year()).isEqualTo(lastMonth.getYear());
            softly.assertThat(result.data().get(0).badges().ranking().month()).isEqualTo(lastMonth.getMonthValue());
            softly.assertThat(result.data().get(1).badges()).isNull();
        });
    }

    @Test
    void 월간_랭킹_조회_시_가장_최근_챌린지_뱃지가_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        
        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));
        
        // 오래된 챌린지 뱃지
        ChallengeBadge oldBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.BRONZE)
                .challengeId(1L)
                .challengeName("오래된 챌린지")
                .challengeGeneration(1)
                .build();
        badgeRepository.save(oldBadge);
        badgeRepository.flush();
        
        // 최근 챌린지 뱃지
        ChallengeBadge recentBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .challengeId(2L)
                .challengeName("최근 챌린지")
                .challengeGeneration(2)
                .build();
        badgeRepository.save(recentBadge);
        
        // when
        MonthlyReadingRankingResponse result = readingService.getMonthlyReadingRank(10);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(1);
            softly.assertThat(result.data().get(0).badges()).isNotNull();
            softly.assertThat(result.data().get(0).badges().challenge()).isNotNull();
            softly.assertThat(result.data().get(0).badges().challenge().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.data().get(0).badges().challenge().name()).isEqualTo("최근 챌린지");
            softly.assertThat(result.data().get(0).badges().challenge().generation()).isEqualTo(2);
        });
    }

    @Test
    void 월간_랭킹_조회_시_랭킹_뱃지와_챌린지_뱃지가_모두_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        
        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));
        
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        RankingBadge rankingBadge = RankingBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .periodYear(lastMonth.getYear())
                .periodMonth(lastMonth.getMonthValue())
                .build();
        badgeRepository.save(rankingBadge);
        
        ChallengeBadge challengeBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.SILVER)
                .challengeId(1L)
                .challengeName("테스트 챌린지")
                .challengeGeneration(1)
                .build();
        badgeRepository.save(challengeBadge);
        
        // when
        MonthlyReadingRankingResponse result = readingService.getMonthlyReadingRank(10);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(1);
            softly.assertThat(result.data().get(0).badges()).isNotNull();
            softly.assertThat(result.data().get(0).badges().ranking()).isNotNull();
            softly.assertThat(result.data().get(0).badges().challenge()).isNotNull();
            softly.assertThat(result.data().get(0).badges().ranking().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.data().get(0).badges().challenge().grade()).isEqualTo(BadgeGrade.SILVER);
        });
    }

    @Test
    void 월간_랭킹_조회_시_뱃지가_없으면_null로_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();
        
        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));
        
        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));
        
        // when
        MonthlyReadingRankingResponse result = readingService.getMonthlyReadingRank(10);
        
        // then
        assertSoftly(softly -> {
            softly.assertThat(result.data()).hasSize(1);
            softly.assertThat(result.data().get(0).badges()).isNull();
        });
    }

    @Test
    void 내_랭킹_조회_시_이전달_랭킹_뱃지가_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        RankingBadge rankingBadge = RankingBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .periodYear(lastMonth.getYear())
                .periodMonth(lastMonth.getMonthValue())
                .build();
        badgeRepository.save(rankingBadge);

        // when
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse result = readingService.getMemberMonthlyReadingRank(member1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.badges()).isNotNull();
            softly.assertThat(result.badges().ranking()).isNotNull();
            softly.assertThat(result.badges().ranking().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.badges().ranking().year()).isEqualTo(lastMonth.getYear());
            softly.assertThat(result.badges().ranking().month()).isEqualTo(lastMonth.getMonthValue());
        });
    }

    @Test
    void 내_랭킹_조회_시_가장_최근_챌린지_뱃지가_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));

        // 오래된 챌린지 뱃지
        ChallengeBadge oldBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.BRONZE)
                .challengeId(1L)
                .challengeName("오래된 챌린지")
                .challengeGeneration(1)
                .build();
        badgeRepository.save(oldBadge);
        badgeRepository.flush();

        // 최근 챌린지 뱃지
        ChallengeBadge recentBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .challengeId(2L)
                .challengeName("최근 챌린지")
                .challengeGeneration(2)
                .build();
        badgeRepository.save(recentBadge);

        // when
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse result = readingService.getMemberMonthlyReadingRank(member1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.badges()).isNotNull();
            softly.assertThat(result.badges().challenge()).isNotNull();
            softly.assertThat(result.badges().challenge().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.badges().challenge().name()).isEqualTo("최근 챌린지");
            softly.assertThat(result.badges().challenge().generation()).isEqualTo(2);
        });
    }

    @Test
    void 내_랭킹_조회_시_랭킹_뱃지와_챌린지_뱃지가_모두_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        RankingBadge rankingBadge = RankingBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.GOLD)
                .periodYear(lastMonth.getYear())
                .periodMonth(lastMonth.getMonthValue())
                .build();
        badgeRepository.save(rankingBadge);

        ChallengeBadge challengeBadge = ChallengeBadge.builder()
                .memberId(member1.getId())
                .grade(BadgeGrade.SILVER)
                .challengeId(1L)
                .challengeName("테스트 챌린지")
                .challengeGeneration(1)
                .build();
        badgeRepository.save(challengeBadge);

        // when
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse result = readingService.getMemberMonthlyReadingRank(member1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.badges()).isNotNull();
            softly.assertThat(result.badges().ranking()).isNotNull();
            softly.assertThat(result.badges().challenge()).isNotNull();
            softly.assertThat(result.badges().ranking().grade()).isEqualTo(BadgeGrade.GOLD);
            softly.assertThat(result.badges().challenge().grade()).isEqualTo(BadgeGrade.SILVER);
        });
    }

    @Test
    void 내_랭킹_조회_시_뱃지가_없으면_null로_표시된다() {
        // given
        monthlyReadingSnapshotRepository.deleteAllInBatch();

        Member member1 = memberRepository.save(TestFixture.createUniqueMember("member1", "provider1"));

        monthlyReadingSnapshotRepository.save(TestFixture.monthlyReadingSnapshotWithRank(member1, 30, 1, 0));

        // when
        readingService.updateMonthlyRanking();
        MemberMonthlyReadingRankResponse result = readingService.getMemberMonthlyReadingRank(member1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.badges()).isNull();
        });
    }

    private RankingBadge findRankingBadge(List<Badge> badges, Long memberId, BadgeGrade grade) {
        return badges.stream()
                .filter(b -> b instanceof RankingBadge)
                .map(b -> (RankingBadge) b)
                .filter(b -> b.getMemberId().equals(memberId) && b.getGrade() == grade)
                .findFirst()
                .orElse(null);
    }
}
