package news.bombomemail.article;

import static org.assertj.core.api.SoftAssertions.*;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import news.bombomemail.email.util.EmailContentExtractor;
import news.bombomemail.member.Gender;
import news.bombomemail.member.Member;
import news.bombomemail.member.MemberRepository;
import news.bombomemail.newsletter.Newsletter;
import news.bombomemail.newsletter.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({ArticleService.class, EmailContentExtractor.class})
class ArticleServiceTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    EmailContentExtractor emailContentExtractor;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    NewsletterRepository newsletterRepository;

    @Autowired
    ArticleRepository articleRepository;

    private Session session;

    @BeforeEach
    void setup() {
        session = Session.getDefaultInstance(new Properties());
        memberRepository.save(Member.builder()
                .email("test-member@example.com")
                .nickname("테스트멤버")
                .gender(Gender.MALE)
                .roleId(1L)
                .build());
        newsletterRepository.save(Newsletter.builder()
                .name("테스트뉴스레터")
                .description("설명")
                .imageUrl("이미지")
                .email("test-newsletter@example.com")
                .categoryId(1L)
                .detailId(1L)
                .build());
    }

    @Test
    void TO_수신자가_없으면_저장되지_않고_false_반환() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("제목만");
        msg.setText("이것은 테스트용 이메일 본문입니다.");
        String content = emailContentExtractor.extractContents(msg);


        // when
        boolean result = articleService.save(msg, content);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isFalse();
            softly.assertThat(articleRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 등록된_회원과_뉴스레터가_있으면_Article_저장_후_true_반환() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("test-member@example.com"));
        msg.setFrom(new InternetAddress("test-newsletter@example.com"));
        msg.setSubject("테스트 이메일 제목");
        msg.setText("이것은 테스트용 이메일 본문입니다.");
        String content = emailContentExtractor.extractContents(msg);


        // when
        boolean result = articleService.save(msg, content);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            List<Article> all = articleRepository.findAll();
            softly.assertThat(all).hasSize(1);
            Article article = all.get(0);
            softly.assertThat(article.getTitle()).isEqualTo("테스트 이메일 제목");
            softly.assertThat(article.getContents()).contains("테스트용 이메일 본문입니다");
            softly.assertThat(article.getExpectedReadTime()).isOne();
            softly.assertThat(article.getContentsSummary()).isEqualTo("이것은 테스트용 이메일 본문입니다.");
        });
    }

    @Test
    void 회원이_등록되어_있지_않으면_저장되지_않고_false_반환() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("no-member@example.com"));
        msg.setFrom(new InternetAddress("test-newsletter@example.com"));
        msg.setSubject("테스트");
        msg.setText("이것은 테스트용 이메일 본문입니다.");
        String content = emailContentExtractor.extractContents(msg);

        // when
        boolean result = articleService.save(msg, content);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isFalse();
            softly.assertThat(articleRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 뉴스레터가_등록되어_있지_않으면_저장되지_않고_false_반환() throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("test-member@example.com"));
        msg.setFrom(new InternetAddress("no-news@example.com"));
        msg.setSubject("테스트");
        msg.setText("이것은 테스트용 이메일 본문입니다.");
        String content = emailContentExtractor.extractContents(msg);

        boolean result = articleService.save(msg, content);

        assertSoftly(softly -> {
            softly.assertThat(result).isFalse();
            softly.assertThat(articleRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 본문이_빈_문자열인_경우_expectedReadTime_0_summary_는_빈_문자열로_저장() throws Exception {
        // given
        MimeMessage msg = new MimeMessage(session);
        msg.addRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("test-member@example.com"));
        msg.setFrom(new InternetAddress("test-newsletter@example.com"));
        msg.setSubject("제목만");
        msg.setText("");
        String content = emailContentExtractor.extractContents(msg);

        // when
        boolean result = articleService.save(msg, content);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            Article a = articleRepository.findAll().get(0);
            softly.assertThat(a.getContents()).isEmpty();
            softly.assertThat(a.getExpectedReadTime()).isZero();
            softly.assertThat(a.getContentsSummary()).isEmpty();
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
        msg.addRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress("test-member@example.com"));
        msg.setFrom(new InternetAddress("test-newsletter@example.com"));
        msg.setSubject("긴본문테스트");
        msg.setText(longBody);
        String content = emailContentExtractor.extractContents(msg);

        // when
        boolean result = articleService.save(msg, content);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
            Article a = articleRepository.findAll().get(0);
            softly.assertThat(a.getContentsSummary()).hasSize(103);
            softly.assertThat(a.getContentsSummary()).endsWith("...");
        });
    }
}
