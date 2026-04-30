package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MaeilMailIssueJobTest {

    @Test
    void fail하면_실패메시지를_최대_1000자로_저장한다() {
        // given
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );
        String failedMessage = "a".repeat(1001);
        LocalDateTime failedAt = LocalDateTime.of(2026, 4, 27, 7, 10);

        // when
        issueJob.fail(failedMessage, failedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.FAILED);
            softly.assertThat(issueJob.getFailedMessage()).hasSize(1000);
            softly.assertThat(issueJob.getFailedAt()).isEqualTo(failedAt);
        });
    }

    @Test
    void 이미_완료된_job을_complete하면_완료시각을_덮어쓰지_않는다() {
        // given
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );
        LocalDateTime firstCompletedAt = LocalDateTime.of(2026, 4, 27, 7, 30);
        LocalDateTime secondCompletedAt = LocalDateTime.of(2026, 4, 27, 8, 0);
        issueJob.complete(firstCompletedAt);

        // when
        issueJob.complete(secondCompletedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.COMPLETED);
            softly.assertThat(issueJob.getCompletedAt()).isEqualTo(firstCompletedAt);
        });
    }

    @Test
    void running이_아닌_job은_complete할_수_없다() {
        // given
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );
        issueJob.fail("fail", LocalDateTime.of(2026, 4, 27, 7, 10));

        // when & then
        assertThatThrownBy(() -> issueJob.complete(LocalDateTime.of(2026, 4, 27, 7, 30)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("실행 중인 매일메일 발행 job만 완료할 수 있습니다.");
    }
}
