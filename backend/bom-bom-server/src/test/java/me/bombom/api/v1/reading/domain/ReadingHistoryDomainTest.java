package me.bombom.api.v1.reading.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReadingHistoryDomainTest {

    @Test
    void ContinueReadingShieldHistory_grant는_보호막_지급_이력을_생성한다() {
        // given
        LocalDate eventDate = LocalDate.of(2026, 1, 1);

        // when
        ContinueReadingShieldHistory history = ContinueReadingShieldHistory.grant(
                1L,
                ContinueReadingShieldHistoryReason.MONTHLY_RESET,
                eventDate,
                3
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(history.getMemberId()).isEqualTo(1L);
            softly.assertThat(history.getType()).isEqualTo(ContinueReadingShieldHistoryType.GRANT);
            softly.assertThat(history.getReason()).isEqualTo(ContinueReadingShieldHistoryReason.MONTHLY_RESET);
            softly.assertThat(history.getEventDate()).isEqualTo(eventDate);
            softly.assertThat(history.getQuantity()).isEqualTo(3);
        });
    }

    @Test
    void ContinueReadingShieldHistory_use는_보호막_사용_이력을_생성한다() {
        // given
        LocalDate eventDate = LocalDate.of(2026, 1, 2);

        // when
        ContinueReadingShieldHistory history = ContinueReadingShieldHistory.use(
                1L,
                ContinueReadingShieldHistoryReason.DAILY_RESET_PROTECTION_USE,
                eventDate,
                1
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(history.getMemberId()).isEqualTo(1L);
            softly.assertThat(history.getType()).isEqualTo(ContinueReadingShieldHistoryType.USE);
            softly.assertThat(history.getReason()).isEqualTo(ContinueReadingShieldHistoryReason.DAILY_RESET_PROTECTION_USE);
            softly.assertThat(history.getEventDate()).isEqualTo(eventDate);
            softly.assertThat(history.getQuantity()).isEqualTo(1);
        });
    }

    @Test
    void 랭킹_스냅샷과_이력은_읽기_랭킹값을_보관한다() {
        // given
        LocalDate period = LocalDate.of(2026, 1, 1);
        LocalDateTime snapshotAt = LocalDateTime.of(2026, 1, 31, 23, 59);

        // when
        ContinueReadingSnapshot continueSnapshot = ContinueReadingSnapshot.create(1L, 12, 3L);
        ContinueReadingRankHistory continueHistory = ContinueReadingRankHistory.builder()
                .memberId(1L)
                .period(period)
                .dayCount(12)
                .rankOrder(3L)
                .build();
        MonthlyReadingRankHistory monthlyHistory = MonthlyReadingRankHistory.builder()
                .memberId(1L)
                .period(period)
                .readCount(20)
                .rankOrder(2L)
                .build();
        ReadingSnapshotMeta meta = ReadingSnapshotMeta.builder()
                .snapshotType(ReadingSnapshotType.MONTHLY)
                .snapshotAt(snapshotAt)
                .build();
        LowestRankWithDifference lowest = LowestRankWithDifference.of(10L, 5L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(continueSnapshot.getMemberId()).isEqualTo(1L);
            softly.assertThat(continueSnapshot.getDayCount()).isEqualTo(12);
            softly.assertThat(continueSnapshot.getRankOrder()).isEqualTo(3L);
            softly.assertThat(continueHistory.getPeriod()).isEqualTo(period);
            softly.assertThat(continueHistory.getDayCount()).isEqualTo(12);
            softly.assertThat(continueHistory.getRankOrder()).isEqualTo(3L);
            softly.assertThat(monthlyHistory.getReadCount()).isEqualTo(20);
            softly.assertThat(monthlyHistory.getRankOrder()).isEqualTo(2L);
            softly.assertThat(meta.getSnapshotType()).isEqualTo(ReadingSnapshotType.MONTHLY);
            softly.assertThat(meta.getSnapshotAt()).isEqualTo(snapshotAt);
            softly.assertThat(lowest.rank()).isEqualTo(10L);
            softly.assertThat(lowest.difference()).isEqualTo(5L);
        });
    }
}
