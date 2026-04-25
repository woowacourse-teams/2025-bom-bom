package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.domain.RecentArticle;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.RecentArticleRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueEntry;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueResultRecorderTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 4, 24);

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private RecentArticleRepository recentArticleRepository;

    @Mock
    private MaeilMailSentContentRepository sentContentRepository;

    @Mock
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Mock
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @InjectMocks
    private MaeilMailIssueResultRecorder resultRecorder;

    @Test
    void 저장할_엔트리와_이전_발행_track이_모두_없으면_아무것도_저장하지_않는다() {
        resultRecorder.record(new PreparedIssueEntries(List.of(), List.of()), ISSUE_DATE);

        verifyNoInteractions(
                articleRepository,
                recentArticleRepository,
                sentContentRepository,
                issueHistoryRepository,
                trackRepository
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void 아티클_recent_article_sent_content_history를_저장하고_발행한_track을_마킹한다() {
        Article article = Article.builder()
                .title("N+1 문제")
                .contents("content")
                .contentsText("contentsText")
                .contentsSummary("summary")
                .expectedReadTime(3)
                .memberId(10L)
                .newsletterId(500L)
                .arrivedDateTime(LocalDateTime.of(2026, 4, 24, 9, 0))
                .build();
        ReflectionTestUtils.setField(article, "id", 10000L);
        MaeilMailSentContent sentContent = MaeilMailSentContent.builder()
                .memberId(10L)
                .topicId(100L)
                .contentId(1000L)
                .build();
        PreparedIssueEntries preparedIssueEntries = new PreparedIssueEntries(
                List.of(new IssueEntry(article, List.of(1L, 2L), sentContent)),
                List.of(9L)
        );
        given(articleRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        resultRecorder.record(preparedIssueEntries, ISSUE_DATE);

        ArgumentCaptor<List<RecentArticle>> recentArticlesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<MaeilMailIssueHistory>> issueHistoriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(articleRepository).saveAll(List.of(article));
        verify(recentArticleRepository).saveAll(recentArticlesCaptor.capture());
        verify(sentContentRepository).saveAll(List.of(sentContent));
        verify(issueHistoryRepository).saveAll(issueHistoriesCaptor.capture());
        verify(trackRepository).markIssuedByIds(List.of(1L, 2L, 9L), ISSUE_DATE);

        assertSoftly(softly -> {
            softly.assertThat(recentArticlesCaptor.getValue()).hasSize(1);
            softly.assertThat(recentArticlesCaptor.getValue().getFirst().getTitle()).isEqualTo("N+1 문제");
            softly.assertThat(issueHistoriesCaptor.getValue()).hasSize(1);
            softly.assertThat(issueHistoriesCaptor.getValue().getFirst().getIssueDate()).isEqualTo(ISSUE_DATE);
            softly.assertThat(issueHistoriesCaptor.getValue().getFirst().getMemberId()).isEqualTo(10L);
            softly.assertThat(issueHistoriesCaptor.getValue().getFirst().getTopicId()).isEqualTo(100L);
        });
    }
}
