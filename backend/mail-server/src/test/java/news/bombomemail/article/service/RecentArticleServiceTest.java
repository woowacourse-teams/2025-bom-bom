package news.bombomemail.article.service;

import static org.assertj.core.api.SoftAssertions.*;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import news.bombomemail.article.domain.RecentArticle;
import news.bombomemail.article.repository.RecentArticleRepository;
import news.bombomemail.email.extractor.EmailContentExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import news.bombomemail.article.util.html.HtmlCleanerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({RecentArticleService.class, HtmlCleanerConfig.class})
class RecentArticleServiceTest {

    @Autowired
    RecentArticleService recentArticleService;

    @Autowired
    RecentArticleRepository recentArticleRepository;

    private Session session;
    private Long memberId;
    private Long newsletterId;

    @BeforeEach
    void setup() {
        session = Session.getDefaultInstance(new Properties());
        memberId = 1L;
        newsletterId = 1L;
        recentArticleRepository.deleteAll();
    }

    @Test
    void RecentArticle_정상적으로_저장() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("테스트 이메일 제목");
        msg.setText("이것은 테스트용 이메일 본문입니다.");
        String content = EmailContentExtractor.extractContents(msg);

        // when
        recentArticleService.save(msg, content, memberId, newsletterId);

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(1);
            RecentArticle recentArticle = all.get(0);
            softly.assertThat(recentArticle.getTitle()).isEqualTo("테스트 이메일 제목");
            softly.assertThat(recentArticle.getContents()).contains("테스트용 이메일 본문입니다");
            softly.assertThat(recentArticle.getContentsText()).contains("테스트용 이메일 본문입니다");
            softly.assertThat(recentArticle.getExpectedReadTime()).isOne();
            softly.assertThat(recentArticle.getContentsSummary()).isEqualTo("이것은 테스트용 이메일 본문입니다.");
            softly.assertThat(recentArticle.getMemberId()).isEqualTo(memberId);
            softly.assertThat(recentArticle.getNewsletterId()).isEqualTo(newsletterId);
            softly.assertThat(recentArticle.getArrivedDateTime()).isNotNull();
        });
    }

    @Test
    void HTML_본문에서_텍스트가_제대로_추출되어_저장() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("HTML 테스트");
        msg.setContent("<html><body><p>이것은 <b>HTML</b> 본문입니다.</p></body></html>", "text/html");
        String content = EmailContentExtractor.extractContents(msg);

        // when
        recentArticleService.save(msg, content, memberId, newsletterId);

        // then
        assertSoftly(softly -> {
            RecentArticle recentArticle = recentArticleRepository.findAll().get(0);
            softly.assertThat(recentArticle.getContents()).contains("<html>");
            softly.assertThat(recentArticle.getContentsText()).isEqualTo("이것은 HTML 본문입니다.");
            softly.assertThat(recentArticle.getContentsText()).doesNotContain("<html>", "<b>", "</b>");
        });
    }

    @Test
    void 본문이_빈_문자열인_경우_expectedReadTime_0_summary_는_빈_문자열로_저장() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("제목만");
        msg.setText("");
        String content = EmailContentExtractor.extractContents(msg);

        // when
        recentArticleService.save(msg, content, memberId, newsletterId);

        // then
        assertSoftly(softly -> {
            RecentArticle recentArticle = recentArticleRepository.findAll().get(0);
            softly.assertThat(recentArticle.getContents()).isEmpty();
            softly.assertThat(recentArticle.getContentsText()).isEmpty();
            softly.assertThat(recentArticle.getExpectedReadTime()).isZero();
            softly.assertThat(recentArticle.getContentsSummary()).isEmpty();
        });
    }

    @Test
    void 긴_본문은_summary가_100자_뒤에_점점점_을_붙여_저장() throws Exception {
        // given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("a");
        }
        String longBody = sb.toString();

        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("긴본문테스트");
        msg.setText(longBody);
        String content = EmailContentExtractor.extractContents(msg);

        // when
        recentArticleService.save(msg, content, memberId, newsletterId);

        // then
        assertSoftly(softly -> {
            RecentArticle recentArticle = recentArticleRepository.findAll().get(0);
            softly.assertThat(recentArticle.getContentsSummary()).hasSize(103);
            softly.assertThat(recentArticle.getContentsSummary()).endsWith("...");
        });
    }

    @Test
    void 여러_RecentArticle_저장_시_모두_저장됨() throws Exception {
        // given
        MimeMessage msg1 = new MimeMessage(session);
        msg1.setSubject("첫 번째 제목");
        msg1.setText("첫 번째 본문");
        String content1 = EmailContentExtractor.extractContents(msg1);

        MimeMessage msg2 = new MimeMessage(session);
        msg2.setSubject("두 번째 제목");
        msg2.setText("두 번째 본문");
        String content2 = EmailContentExtractor.extractContents(msg2);

        // when
        recentArticleService.save(msg1, content1, memberId, newsletterId);
        recentArticleService.save(msg2, content2, memberId, newsletterId);

        // then
        assertSoftly(softly -> {
            List<RecentArticle> all = recentArticleRepository.findAll();
            softly.assertThat(all).hasSize(2);
            softly.assertThat(all).extracting(RecentArticle::getTitle)
                    .containsExactly("첫 번째 제목", "두 번째 제목");
        });
    }
}
