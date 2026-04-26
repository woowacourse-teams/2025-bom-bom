package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.event.ArticleArrivedEvent;
import news.bombomemail.article.event.ArticleSource;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueEntry;
import news.bombomemail.nativenewsletter.maeilmail.dto.PreparedIssueEntries;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssuePublisherTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private NewsletterRepository newsletterRepository;

    @Mock
    private MaeilMailSentContentRepository sentContentRepository;

    @Mock
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Mock
    private MaeilMailSubscriptionTrackRepository trackRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MaeilMailIssuePublisher issuePublisher;

    @BeforeEach
    void setup() {
        issuePublisher = new MaeilMailIssuePublisher(
                articleRepository,
                newsletterRepository,
                sentContentRepository,
                issueHistoryRepository,
                trackRepository,
                eventPublisher
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void article을_저장하고_매일메일_발행_이벤트와_발행결과를_기록한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        Article article = createArticle(100L, 50L, 1L, "매일메일 제목", "<p>본문</p>");
        MaeilMailSentContent sentContent = createSentContent(1L, 10L, 9000L);
        IssueEntry entry = new IssueEntry(article, List.of(1L, 2L), sentContent);
        PreparedIssueEntries preparedIssueEntries = new PreparedIssueEntries(List.of(entry), List.of(9L));
        Newsletter newsletter = createNewsletter(50L, "매일메일");

        given(articleRepository.saveAll(List.of(article))).willReturn(List.of(article));
        given(newsletterRepository.findAllById(Set.of(50L))).willReturn(List.of(newsletter));

        // when
        issuePublisher.publish(preparedIssueEntries, issueDate);

        // then
        ArgumentCaptor<ArticleArrivedEvent> eventCaptor = ArgumentCaptor.forClass(ArticleArrivedEvent.class);
        ArgumentCaptor<Iterable> historyCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        verify(sentContentRepository).saveAll(List.of(sentContent));
        verify(issueHistoryRepository).saveAll(historyCaptor.capture());
        verify(trackRepository).markIssuedByIds(List.of(1L, 2L, 9L), issueDate);

        ArticleArrivedEvent event = eventCaptor.getValue();
        List<MaeilMailIssueHistory> histories = StreamSupport.stream(historyCaptor.getValue().spliterator(), false)
                .map(MaeilMailIssueHistory.class::cast)
                .toList();
        assertSoftly(softly -> {
            softly.assertThat(event.newsletterId()).isEqualTo(50L);
            softly.assertThat(event.newsletterName()).isEqualTo("매일메일");
            softly.assertThat(event.articleId()).isEqualTo(100L);
            softly.assertThat(event.articleTitle()).isEqualTo("매일메일 제목");
            softly.assertThat(event.memberId()).isEqualTo(1L);
            softly.assertThat(event.unsubscribeUrl()).isNull();
            softly.assertThat(event.contents()).isEqualTo("<p>본문</p>");
            softly.assertThat(event.source()).isEqualTo(ArticleSource.MAEIL_MAIL_ISSUED);
            softly.assertThat(histories).hasSize(1);
            softly.assertThat(histories.getFirst().getArticleId()).isEqualTo(100L);
            softly.assertThat(histories.getFirst().getContentId()).isEqualTo(9000L);
        });
    }

    @Test
    void 저장된_article_수와_entry_수가_다르면_예외가_발생한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        Article article = createArticle(null, 50L, 1L, "매일메일 제목", "<p>본문</p>");
        MaeilMailSentContent sentContent = createSentContent(1L, 10L, 9000L);
        IssueEntry entry = new IssueEntry(article, List.of(1L), sentContent);
        PreparedIssueEntries preparedIssueEntries = new PreparedIssueEntries(List.of(entry), List.of());

        given(articleRepository.saveAll(List.of(article))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> issuePublisher.publish(preparedIssueEntries, issueDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Article 저장 결과 수");
        verifyNoInteractions(newsletterRepository, sentContentRepository, issueHistoryRepository, trackRepository, eventPublisher);
    }

    @Test
    void 이미_발행된_track만_있으면_article_저장없이_track만_갱신한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        PreparedIssueEntries preparedIssueEntries = new PreparedIssueEntries(List.of(), List.of(9L, 10L));

        // when
        issuePublisher.publish(preparedIssueEntries, issueDate);

        // then
        verify(trackRepository).markIssuedByIds(List.of(9L, 10L), issueDate);
        verifyNoInteractions(articleRepository, newsletterRepository, sentContentRepository, issueHistoryRepository, eventPublisher);
    }

    private Article createArticle(
            Long id,
            Long newsletterId,
            Long memberId,
            String title,
            String contents
    ) {
        return Article.builder()
                .id(id)
                .title(title)
                .contents(contents)
                .contentsText("본문")
                .contentsSummary("요약")
                .expectedReadTime(1)
                .memberId(memberId)
                .newsletterId(newsletterId)
                .arrivedDateTime(LocalDateTime.of(2026, 4, 27, 7, 0))
                .build();
    }

    private MaeilMailSentContent createSentContent(Long memberId, Long topicId, Long contentId) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }

    private Newsletter createNewsletter(Long id, String name) {
        return Newsletter.builder()
                .id(id)
                .name(name)
                .description("설명")
                .imageUrl("image")
                .email("maeil@example.com")
                .categoryId(1L)
                .detailId(1L)
                .build();
    }
}
