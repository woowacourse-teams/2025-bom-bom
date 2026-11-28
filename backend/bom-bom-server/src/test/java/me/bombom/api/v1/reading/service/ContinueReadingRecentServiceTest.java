package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResult;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResultType;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryReason;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.domain.StreakDayStatus;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.dto.response.RecentContinueReadingResponse;
import me.bombom.api.v1.reading.dto.response.StreakDayResponse;
import me.bombom.api.v1.reading.dto.response.StreakShieldStatus;
import me.bombom.api.v1.reading.repository.ContinueReadingDailyResultRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingShieldRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class ContinueReadingRecentServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    // 2026-11-27(금) 12:00 KST
    private static final Instant NOW = Instant.parse("2026-11-27T03:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 27);

    @Autowired
    private ContinueReadingRecentService continueReadingRecentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContinueReadingRealtimeRepository continueReadingRealtimeRepository;

    @Autowired
    private ContinueReadingSnapshotRepository continueReadingSnapshotRepository;

    @Autowired
    private ContinueReadingShieldRepository continueReadingShieldRepository;

    @Autowired
    private ContinueReadingShieldHistoryRepository continueReadingShieldHistoryRepository;

    @Autowired
    private ContinueReadingDailyResultRepository continueReadingDailyResultRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Autowired
    private ArticleReadHistoryRepository articleReadHistoryRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock clock;

    private long articleIdSequence = 1L;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW, SEOUL_ZONE);
    }

    @Test
    void 최근_7일의_날짜별_상태와_연속_읽기_현황을_조회한다() {
        // given
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        saveContinueReading(member, 3, 1L);
        saveTodayReading(member, 0);

        saveRead(member, LocalDateTime.of(2026, 11, 20, 23, 59, 59)); // 조회 범위 밖
        saveRead(member, LocalDateTime.of(2026, 11, 22, 10, 0));      // 일요일에 읽음
        saveRead(member, LocalDateTime.of(2026, 11, 26, 0, 0));
        saveRead(member, LocalDateTime.of(2026, 11, 26, 23, 59, 59)); // 같은 날 여러 번 읽음
        saveShieldUse(member, LocalDate.of(2026, 11, 23));
        continueReadingShieldHistoryRepository.save(ContinueReadingShieldHistory.grant(
                member.getId(),
                ContinueReadingShieldHistoryReason.MONTHLY_RESET,
                LocalDate.of(2026, 11, 25),
                1
        ));
        holidayRepository.save(Holiday.builder()
                .date(LocalDate.of(2026, 11, 24))
                .name("테스트공휴일")
                .build());

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 7);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayCount()).isEqualTo(3);
            softly.assertThat(response.rank()).isEqualTo(1L);
            softly.assertThat(response.streakShield().status()).isEqualTo(StreakShieldStatus.AVAILABLE);
            softly.assertThat(response.streakShield().remainingCount()).isEqualTo(1);
            softly.assertThat(response.days())
                    .extracting(StreakDayResponse::date, StreakDayResponse::status)
                    .containsExactly(
                            tuple(LocalDate.of(2026, 11, 21), StreakDayStatus.REST),
                            tuple(LocalDate.of(2026, 11, 22), StreakDayStatus.READ),
                            tuple(LocalDate.of(2026, 11, 23), StreakDayStatus.SHIELD),
                            tuple(LocalDate.of(2026, 11, 24), StreakDayStatus.REST),
                            tuple(LocalDate.of(2026, 11, 25), StreakDayStatus.MISSED), // 보호막 GRANT는 무시
                            tuple(LocalDate.of(2026, 11, 26), StreakDayStatus.READ),
                            tuple(TODAY, StreakDayStatus.PENDING)
                    );
        });
    }

    @Test
    void 자정에_기록한_일별_결과로_도착_없는_평일과_읽은_기록_없는_읽음을_판정한다() {
        // given
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        saveContinueReading(member, 5, 1L);
        saveTodayReading(member, 0);
        saveDailyResult(member, LocalDate.of(2026, 11, 25), ContinueReadingDailyResultType.NO_ARTICLE);
        saveDailyResult(member, LocalDate.of(2026, 11, 26), ContinueReadingDailyResultType.READ); // 가이드 메일 등

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 3);

        // then
        assertThat(response.days())
                .extracting(StreakDayResponse::date, StreakDayResponse::status)
                .containsExactly(
                        tuple(LocalDate.of(2026, 11, 25), StreakDayStatus.NO_ARTICLE),
                        tuple(LocalDate.of(2026, 11, 26), StreakDayStatus.READ),
                        tuple(TODAY, StreakDayStatus.PENDING)
                );
    }

    @Test
    void 오늘_0시에_읽었으면_오늘은_READ다() {
        // given
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        saveContinueReading(member, 1, 1L);
        saveTodayReading(member, 0);
        saveRead(member, TODAY.atStartOfDay());

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 1);

        // then
        assertThat(response.days())
                .extracting(StreakDayResponse::date, StreakDayResponse::status)
                .containsExactly(tuple(TODAY, StreakDayStatus.READ));
    }

    @Test
    void 가입_당일_가이드_메일만_읽어도_오늘은_READ다() {
        // given: 가이드 메일 읽기는 article_read_history 없이 today_reading.read_count만 올린다
        Member member = saveMemberJoinedAt(TODAY.atTime(9, 0));
        saveContinueReading(member, 1, 3L);
        saveTodayReading(member, 1);

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 2);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayCount()).isEqualTo(1);
            softly.assertThat(response.days())
                    .extracting(StreakDayResponse::status)
                    .containsExactly(StreakDayStatus.BEFORE_JOIN, StreakDayStatus.READ);
        });
    }

    @Test
    void 가입_직후_이력이_없는_회원은_가입일_이전이_BEFORE_JOIN이고_연속_일수는_0이다() {
        // given
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 11, 26, 21, 0));
        saveContinueReading(member, 0, 5L);

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 7);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayCount()).isZero();
            softly.assertThat(response.rank()).isEqualTo(5L);
            softly.assertThat(response.days())
                    .extracting(StreakDayResponse::status)
                    .containsExactly(
                            StreakDayStatus.BEFORE_JOIN,
                            StreakDayStatus.BEFORE_JOIN,
                            StreakDayStatus.BEFORE_JOIN,
                            StreakDayStatus.BEFORE_JOIN,
                            StreakDayStatus.BEFORE_JOIN,
                            StreakDayStatus.MISSED,
                            StreakDayStatus.PENDING
                    );
        });
    }

    @Test
    void 연속_일수는_실시간_값이고_순위는_스냅샷_값이다() {
        // given: 스냅샷 갱신 전에 실시간 연속 일수가 먼저 오른 상황
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        continueReadingRealtimeRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(4)
                .build());
        continueReadingShieldRepository.save(ContinueReadingShield.create(member.getId()));
        continueReadingSnapshotRepository.save(ContinueReadingSnapshot.create(member.getId(), 3, 7L));

        // when
        RecentContinueReadingResponse response = continueReadingRecentService.getRecentContinueReading(member, 1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.dayCount()).isEqualTo(4);
            softly.assertThat(response.rank()).isEqualTo(7L);
        });
    }

    @Test
    void 연속_읽기_스냅샷이_없으면_ENTITY_NOT_FOUND_예외가_발생한다() {
        // given
        Member member = saveMemberJoinedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        continueReadingRealtimeRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(0)
                .build());

        // when & then
        assertThatThrownBy(() -> continueReadingRecentService.getRecentContinueReading(member, 7))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    private Member saveMemberJoinedAt(LocalDateTime joinedAt) {
        Member member = memberRepository.save(TestFixture.createUniqueMember("recent-streak", "recent-streak"));
        jdbcTemplate.update("UPDATE member SET created_at = ? WHERE id = ?", joinedAt, member.getId());
        return memberRepository.findById(member.getId()).orElseThrow();
    }

    private void saveContinueReading(Member member, int dayCount, long rankOrder) {
        continueReadingRealtimeRepository.save(ContinueReadingRealtime.builder()
                .memberId(member.getId())
                .dayCount(dayCount)
                .build());
        continueReadingShieldRepository.save(ContinueReadingShield.create(member.getId()));
        continueReadingSnapshotRepository.save(ContinueReadingSnapshot.create(member.getId(), dayCount, rankOrder));
    }

    private void saveTodayReading(Member member, int readCount) {
        todayReadingRepository.save(TodayReading.builder()
                .memberId(member.getId())
                .totalCount(1)
                .currentCount(readCount)
                .readCount(readCount)
                .build());
    }

    private void saveRead(Member member, LocalDateTime readAt) {
        articleReadHistoryRepository.save(ArticleReadHistory.builder()
                .memberId(member.getId())
                .articleId(articleIdSequence++)
                .newsletterId(1L)
                .categoryId(1L)
                .readAt(readAt)
                .build());
    }

    private void saveShieldUse(Member member, LocalDate date) {
        continueReadingShieldHistoryRepository.save(ContinueReadingShieldHistory.use(
                member.getId(),
                ContinueReadingShieldHistoryReason.DAILY_RESET_PROTECTION_USE,
                date,
                1
        ));
    }

    private void saveDailyResult(Member member, LocalDate date, ContinueReadingDailyResultType type) {
        continueReadingDailyResultRepository.save(ContinueReadingDailyResult.builder()
                .memberId(member.getId())
                .resultDate(date)
                .result(type)
                .build());
    }
}
