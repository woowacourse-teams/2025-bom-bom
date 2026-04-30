package news.bombomemail.nativenewsletter.maeilmail.repository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MaeilMailIssueJobRepositoryTest {

    @Autowired
    private MaeilMailIssueJobRepository issueJobRepository;

    @Test
    void 미완료_job_재개_조회는_오늘_issueDate에_해당하는_job만_조회한다() {
        // given
        LocalDate today = LocalDate.of(2026, 4, 27);
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob yesterdayJob = issueJobRepository.save(MaeilMailIssueJob.start(
                yesterday,
                10L,
                startedAt.minusDays(1)
        ));
        MaeilMailIssueJob todayJob = issueJobRepository.save(MaeilMailIssueJob.start(
                today,
                20L,
                startedAt
        ));

        // when
        Optional<MaeilMailIssueJob> result = issueJobRepository.findByIssueDateAndStatusIn(today, List.of(
                MaeilMailIssueJobStatus.RUNNING,
                MaeilMailIssueJobStatus.FAILED
        ));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isPresent();
            softly.assertThat(result.get().getId()).isEqualTo(todayJob.getId());
            softly.assertThat(result.get().getId()).isNotEqualTo(yesterdayJob.getId());
            softly.assertThat(result.get().getIssueDate()).isEqualTo(today);
        });
    }

    @Test
    void 오늘_job이어도_completed이면_미완료_재개_조회에서_제외한다() {
        // given
        LocalDate today = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob completedJob = MaeilMailIssueJob.start(today, 20L, startedAt);
        completedJob.complete(startedAt.plusMinutes(10));
        issueJobRepository.save(completedJob);

        // when
        Optional<MaeilMailIssueJob> result = issueJobRepository.findByIssueDateAndStatusIn(today, List.of(
                MaeilMailIssueJobStatus.RUNNING,
                MaeilMailIssueJobStatus.FAILED
        ));

        // then
        assertSoftly(softly -> softly.assertThat(result).isEmpty());
    }
}
