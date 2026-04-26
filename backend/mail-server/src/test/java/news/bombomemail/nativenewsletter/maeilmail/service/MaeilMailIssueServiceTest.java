package news.bombomemail.nativenewsletter.maeilmail.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueChunkProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Mock
    private MaeilMailIssueChunkProcessor chunkProcessor;

    private MaeilMailIssueService issueService;

    @BeforeEach
    void setup() {
        issueService = new MaeilMailIssueService(clockAt(LocalDateTime.of(2026, 4, 27, 7, 0)), chunkProcessor);
        ReflectionTestUtils.setField(issueService, "issueChunkSize", 200);
    }

    @Test
    void 주말이면_발행하지_않는다() {
        // given
        MaeilMailIssueService weekendIssueService = new MaeilMailIssueService(
                clockAt(LocalDateTime.of(2026, 4, 26, 7, 0)),
                chunkProcessor
        );

        // when
        weekendIssueService.issue();

        // then
        verifyNoInteractions(chunkProcessor);
    }

    @Test
    void 평일이면_chunk가_없을_때까지_순차적으로_처리한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(chunkProcessor.process(issueDate, 0L, pageRequest))
                .willReturn(IssueChunkResult.of(10L, 2, 1, 0));
        given(chunkProcessor.process(issueDate, 10L, pageRequest))
                .willReturn(IssueChunkResult.empty());

        // when
        issueService.issue();

        // then
        verify(chunkProcessor).process(issueDate, 0L, pageRequest);
        verify(chunkProcessor).process(issueDate, 10L, pageRequest);
    }

    @Test
    void chunkSize가_1보다_작으면_1로_보정한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        PageRequest pageRequest = PageRequest.of(0, 1);
        ReflectionTestUtils.setField(issueService, "issueChunkSize", 0);
        given(chunkProcessor.process(issueDate, 0L, pageRequest))
                .willReturn(IssueChunkResult.empty());

        // when
        issueService.issue();

        // then
        verify(chunkProcessor).process(issueDate, 0L, pageRequest);
    }

    private Clock clockAt(LocalDateTime localDateTime) {
        return Clock.fixed(localDateTime.atZone(SEOUL).toInstant(), SEOUL);
    }
}
