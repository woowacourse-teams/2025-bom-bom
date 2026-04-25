package news.bombomemail.article.event;

import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;
import news.bombomemail.article.domain.RecentArticle;
import news.bombomemail.article.repository.RecentArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class RecentArticleEventListenerTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    RecentArticleRepository recentArticleRepository;

    @BeforeEach
    void setup() {
        recentArticleRepository.deleteAll();
    }

    @Test
    @Transactional
    void afterCommit_이벤트_발행후_RecentArticle_저장() throws Exception {
        // given
        Long newsletterId = 1L;
        String newsletterName = "테스트 뉴스레터";
        Long articleId = 1L;
        String articleTitle = "테스트 아티클";
        Long memberId = 1L;
        String unsubscribeUrl = "unsubscribeUrl";
        String contents = "이것은 테스트용 이메일 본문입니다.";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, unsubscribeUrl, contents,
                ArticleSource.EMAIL_RECEIVED));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(1);
            RecentArticle recentArticle = all.get(0);
            softly.assertThat(recentArticle.getArticleId()).isEqualTo(articleId);
            softly.assertThat(recentArticle.getTitle()).isEqualTo(articleTitle);
            softly.assertThat(recentArticle.getContents()).isEqualTo(contents);
            softly.assertThat(recentArticle.getMemberId()).isEqualTo(memberId);
            softly.assertThat(recentArticle.getNewsletterId()).isEqualTo(newsletterId);
        });
    }

    @Test
    @Transactional
    void 매일메일_발행_이벤트도_RecentArticle을_저장한다() {
        // given
        Long newsletterId = 1L;
        String newsletterName = "매일메일";
        Long articleId = 10L;
        String articleTitle = "매일메일 발행 제목";
        Long memberId = 2L;
        String contents = "<p>매일메일 본문입니다.</p>";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, null, contents,
                ArticleSource.MAEIL_MAIL_ISSUED));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(1);
            RecentArticle recentArticle = all.get(0);
            softly.assertThat(recentArticle.getArticleId()).isEqualTo(articleId);
            softly.assertThat(recentArticle.getTitle()).isEqualTo(articleTitle);
            softly.assertThat(recentArticle.getContents()).isEqualTo(contents);
            softly.assertThat(recentArticle.getMemberId()).isEqualTo(memberId);
            softly.assertThat(recentArticle.getNewsletterId()).isEqualTo(newsletterId);
        });
    }

    @Test
    @Transactional
    void 여러_이벤트_발행시_모든_RecentArticle_저장() throws Exception {
        // given
        Long newsletterId = 1L;
        String newsletterName = "테스트 뉴스레터";
        Long memberId = 1L;
        String unsubscribeUrl = "unsubscribeUrl";

        String contents1 = "첫 번째 본문";

        String contents2 = "두 번째 본문";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, 1L, "첫 번째 제목", memberId, unsubscribeUrl, contents1,
                ArticleSource.EMAIL_RECEIVED));
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, 2L, "두 번째 제목", memberId, unsubscribeUrl, contents2,
                ArticleSource.EMAIL_RECEIVED));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(2);
            softly.assertThat(all).extracting(RecentArticle::getTitle)
                    .containsExactly("첫 번째 제목", "두 번째 제목");
            softly.assertThat(all).extracting(RecentArticle::getArticleId)
                    .containsExactly(1L, 2L);
        });
    }
}
