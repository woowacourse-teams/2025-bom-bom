package news.bombomemail.nativenewsletter.maeilmail.repository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailContent;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MaeilMailMemberTopicRepositoryTest {

    @Autowired
    private MaeilMailSentContentRepository sentContentRepository;

    @Autowired
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @BeforeEach
    void setup() {
        issueHistoryRepository.deleteAll();
        sentContentRepository.deleteAll();
        contentRepository.deleteAll();
        articleRepository.deleteAll();
    }

    @Test
    void sent_content는_member_topic_pair를_정확히_조회한다() {
        // given
        MaeilMailSentContent firstTarget = createSentContent(1L, 10L, 100L);
        MaeilMailSentContent secondTarget = createSentContent(2L, 20L, 200L);
        MaeilMailSentContent firstCrossProduct = createSentContent(1L, 20L, 300L);
        MaeilMailSentContent secondCrossProduct = createSentContent(2L, 10L, 400L);
        sentContentRepository.saveAll(List.of(
                firstTarget,
                secondTarget,
                firstCrossProduct,
                secondCrossProduct
        ));

        // when
        List<MaeilMailSentContent> sentContents = sentContentRepository.findAllByMemberTopicKeys(List.of(
                new MemberTopicKey(1L, 10L),
                new MemberTopicKey(2L, 20L)
        ));

        // then
        assertSoftly(softly -> {
            softly.assertThat(sentContents).hasSize(2);
            softly.assertThat(sentContents)
                    .extracting(MaeilMailSentContent::getContentId)
                    .containsExactlyInAnyOrder(100L, 200L);
        });
    }

    @Test
    void issue_history는_article_arrivedDateTime과_content_topic으로_member_topic_pair를_조회한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 26);
        issueHistoryRepository.saveAll(List.of(
                createIssueHistory(issueDate, 1L, 10L),
                createIssueHistory(issueDate, 2L, 20L),
                createIssueHistory(issueDate, 1L, 20L),
                createIssueHistory(issueDate, 2L, 10L),
                createIssueHistory(issueDate.minusDays(1), 3L, 30L)
        ));

        // when
        Set<MemberTopicKey> issuedKeys = issueHistoryRepository.findIssuedMemberTopicKeys(issueDate, List.of(
                new MemberTopicKey(1L, 10L),
                new MemberTopicKey(2L, 20L),
                new MemberTopicKey(3L, 30L)
        ));

        // then
        assertSoftly(softly -> {
            softly.assertThat(issuedKeys).hasSize(2);
            softly.assertThat(issuedKeys).containsExactlyInAnyOrder(
                    new MemberTopicKey(1L, 10L),
                    new MemberTopicKey(2L, 20L)
            );
        });
    }

    @Test
    void issue_history는_100개_이상의_member_topic_key를_정확히_조회한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 26);
        List<MemberTopicKey> keys = new ArrayList<>();
        List<MaeilMailIssueHistory> histories = new ArrayList<>();
        for (long i = 1; i <= 120; i++) {
            keys.add(new MemberTopicKey(i, i + 1000));
            if (i % 2 == 0) {
                histories.add(createIssueHistory(issueDate, i, i + 1000));
            }
        }
        issueHistoryRepository.saveAll(histories);

        // when
        Set<MemberTopicKey> issuedKeys = issueHistoryRepository.findIssuedMemberTopicKeys(issueDate, keys);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issuedKeys).hasSize(60);
            softly.assertThat(issuedKeys).allMatch(key -> key.memberId() % 2 == 0);
        });
    }

    private MaeilMailSentContent createSentContent(Long memberId, Long topicId, Long contentId) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }

    private MaeilMailIssueHistory createIssueHistory(LocalDate issueDate, Long memberId, Long topicId) {
        Article article = articleRepository.save(createArticle(memberId, issueDate.atTime(7, 0)));
        MaeilMailContent content = contentRepository.save(createContent(topicId));
        return MaeilMailIssueHistory.builder()
                .articleId(article.getId())
                .contentId(content.getId())
                .build();
    }

    private Article createArticle(Long memberId, LocalDateTime arrivedDateTime) {
        return Article.builder()
                .title("매일메일 제목")
                .contents("<p>본문</p>")
                .contentsText("본문")
                .contentsSummary("요약")
                .expectedReadTime(1)
                .memberId(memberId)
                .newsletterId(1L)
                .arrivedDateTime(arrivedDateTime)
                .build();
    }

    private MaeilMailContent createContent(Long topicId) {
        return MaeilMailContent.builder()
                .topicId(topicId)
                .title("매일메일 제목")
                .content("<p>본문</p>")
                .contentsText("본문")
                .contentsSummary("요약")
                .expectedReadTime(1)
                .build();
    }
}
