package news.bombomemail.article.event;

import static org.assertj.core.api.SoftAssertions.*;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
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

    private Session session;

    @BeforeEach
    void setup() {
        session = Session.getDefaultInstance(new Properties());
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
        MimeMessage message = new MimeMessage(session);
        message.setSubject("테스트 이메일 제목");
        message.setText("이것은 테스트용 이메일 본문입니다.");
        String contents = "이것은 테스트용 이메일 본문입니다.";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, unsubscribeUrl, message, contents));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(1);
            RecentArticle recentArticle = all.get(0);
            softly.assertThat(recentArticle.getTitle()).isEqualTo("테스트 이메일 제목");
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

        MimeMessage message1 = new MimeMessage(session);
        message1.setSubject("첫 번째 제목");
        message1.setText("첫 번째 본문");
        String contents1 = "첫 번째 본문";

        MimeMessage message2 = new MimeMessage(session);
        message2.setSubject("두 번째 제목");
        message2.setText("두 번째 본문");
        String contents2 = "두 번째 본문";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, 1L, "첫 번째 제목", memberId, unsubscribeUrl, message1, contents1));
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, 2L, "두 번째 제목", memberId, unsubscribeUrl, message2, contents2));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(2);
            softly.assertThat(all).extracting(RecentArticle::getTitle)
                    .containsExactly("첫 번째 제목", "두 번째 제목");
        });
    }
}
