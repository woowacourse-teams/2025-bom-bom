package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntrySelectionResult.PendingEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueEntryAssemblerTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-04-24T00:00:00Z");

    @Mock
    private MaeilMailContentRepository contentRepository;

    private MaeilMailIssueEntryAssembler entryAssembler;

    @BeforeEach
    void setUp() {
        entryAssembler = new MaeilMailIssueEntryAssembler(Clock.fixed(NOW, SEOUL_ZONE), contentRepository);
    }

    @Test
    void pending_entry를_아티클과_발송기록으로_조립한다() {
        MaeilMailContent content = createContent(1000L, 100L, "N+1 문제");
        IssueEntrySelectionResult selection = new IssueEntrySelectionResult(
                List.of(new PendingEntry(10L, 100L, 500L, 1000L, List.of(1L, 2L))),
                List.of(9L)
        );
        given(contentRepository.findAllById(List.of(1000L))).willReturn(List.of(content));

        PreparedIssueEntries preparedEntries = entryAssembler.assemble(selection);

        assertSoftly(softly -> {
            softly.assertThat(preparedEntries.entries()).hasSize(1);
            softly.assertThat(preparedEntries.previouslyIssuedTrackIds()).containsExactly(9L);
            softly.assertThat(preparedEntries.entries().getFirst().article().getTitle()).isEqualTo("N+1 문제");
            softly.assertThat(preparedEntries.entries().getFirst().article().getMemberId()).isEqualTo(10L);
            softly.assertThat(preparedEntries.entries().getFirst().article().getNewsletterId()).isEqualTo(500L);
            softly.assertThat(preparedEntries.entries().getFirst().article().getArrivedDateTime())
                    .isEqualTo(LocalDateTime.ofInstant(NOW, SEOUL_ZONE));
            softly.assertThat(preparedEntries.entries().getFirst().trackIds()).containsExactly(1L, 2L);
            softly.assertThat(preparedEntries.entries().getFirst().sentContent().getMemberId()).isEqualTo(10L);
            softly.assertThat(preparedEntries.entries().getFirst().sentContent().getTopicId()).isEqualTo(100L);
            softly.assertThat(preparedEntries.entries().getFirst().sentContent().getContentId()).isEqualTo(1000L);
        });
    }

    @Test
    void 조회되지_않은_컨텐츠의_pending_entry는_건너뛴다() {
        MaeilMailContent content = createContent(1000L, 100L, "N+1 문제");
        IssueEntrySelectionResult selection = new IssueEntrySelectionResult(
                List.of(
                        new PendingEntry(10L, 100L, 500L, 1000L, List.of(1L)),
                        new PendingEntry(20L, 200L, 500L, 2000L, List.of(2L))
                ),
                List.of()
        );
        given(contentRepository.findAllById(List.of(1000L, 2000L))).willReturn(List.of(content));

        PreparedIssueEntries preparedEntries = entryAssembler.assemble(selection);

        assertSoftly(softly -> {
            softly.assertThat(preparedEntries.entries()).hasSize(1);
            softly.assertThat(preparedEntries.entries().getFirst().article().getMemberId()).isEqualTo(10L);
        });
    }

    private MaeilMailContent createContent(Long id, Long topicId, String title) {
        MaeilMailContent content = MaeilMailContent.builder()
                .topicId(topicId)
                .title(title)
                .content("content")
                .contentsText("contentsText")
                .contentsSummary("summary")
                .expectedReadTime(3)
                .build();
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }
}
