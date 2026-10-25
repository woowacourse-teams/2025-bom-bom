package news.bombomemail.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.article.repository.ReceivedMailRepository;
import news.bombomemail.article.service.ArticleService;
import news.bombomemail.article.service.ReceivedMailService;
import news.bombomemail.article.service.ReceivedMailService.DuplicateEmailException;
import news.bombomemail.article.service.ReceivedMailService.ReceivedMailKey;
import news.bombomemail.article.util.UnsubscribeUrlExtractor;
import news.bombomemail.article.util.html.HtmlCleanerConfig;
import news.bombomemail.email.extractor.EmailContentExtractor;
import news.bombomemail.member.domain.Gender;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import news.bombomemail.subscribe.service.UnsubscribePatternReloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "mail.dedup.enabled=true")
@Import({
        ArticleService.class,
        ReceivedMailService.class,
        HtmlCleanerConfig.class,
        UnsubscribeUrlExtractor.class,
        UnsubscribePatternReloadService.class
})
class ReceivedMailDedupTest {

    private static final String NEWSLETTER_EMAIL = "test-newsletter@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";
    private static final String OTHER_MEMBER_EMAIL = "other-member@example.com";

    @Autowired
    ArticleService articleService;

    @Autowired
    ReceivedMailService receivedMailService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ReceivedMailRepository receivedMailRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    NewsletterRepository newsletterRepository;

    private final Session session = Session.getDefaultInstance(new Properties());

    @BeforeEach
    void setUp() {
        saveMember(MEMBER_EMAIL, "회원");
        saveMember(OTHER_MEMBER_EMAIL, "다른회원");
        newsletterRepository.save(Newsletter.builder()
                .name("테스트뉴스레터")
                .description("설명")
                .imageUrl("이미지")
                .email(NEWSLETTER_EMAIL)
                .categoryId(1L)
                .detailId(1L)
                .build());
    }

    @Test
    void 같은_회원에게_같은_Message_ID_메일이_다시_오면_저장하지_않는다() throws Exception {
        // given
        MimeMessage first = message(MEMBER_EMAIL, "<same@newsletter.example>");
        MimeMessage resent = message(MEMBER_EMAIL, "<same@newsletter.example>");
        articleService.save(first, EmailContentExtractor.extractContents(first));

        // when & then
        assertThatThrownBy(() -> articleService.save(resent, EmailContentExtractor.extractContents(resent)))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("<same@newsletter.example>");
        assertThat(articleRepository.count()).isOne();
        assertThat(receivedMailRepository.count()).isOne();
    }

    @Test
    void 같은_Message_ID라도_받는_회원이_다르면_각각_저장한다() throws Exception {
        // given
        MimeMessage toMember = message(MEMBER_EMAIL, "<same@newsletter.example>");
        MimeMessage toOtherMember = message(OTHER_MEMBER_EMAIL, "<same@newsletter.example>");

        // when
        articleService.save(toMember, EmailContentExtractor.extractContents(toMember));
        articleService.save(toOtherMember, EmailContentExtractor.extractContents(toOtherMember));

        // then
        assertThat(articleRepository.count()).isEqualTo(2);
        assertThat(receivedMailRepository.count()).isEqualTo(2);
    }

    @Test
    void Message_ID가_없는_메일은_중복_확인_없이_저장한다() throws Exception {
        // given
        MimeMessage first = message(MEMBER_EMAIL, null);
        MimeMessage second = message(MEMBER_EMAIL, null);

        // when
        articleService.save(first, EmailContentExtractor.extractContents(first));
        articleService.save(second, EmailContentExtractor.extractContents(second));

        // then
        assertThat(articleRepository.count()).isEqualTo(2);
        assertThat(receivedMailRepository.count()).isZero();
    }

    @Test
    void Message_ID는_접힌_헤더와_앞뒤_공백을_정리해서_비교한다() throws Exception {
        // given
        MimeMessage first = message(MEMBER_EMAIL, "<folded@newsletter.example>");
        MimeMessage resent = message(MEMBER_EMAIL, "\r\n <folded@newsletter.example> ");
        articleService.save(first, EmailContentExtractor.extractContents(first));

        // when & then
        assertThatThrownBy(() -> articleService.save(resent, EmailContentExtractor.extractContents(resent)))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 확인_뒤_다른_서버가_먼저_저장하면_PK_충돌을_중복으로_처리한다() throws Exception {
        // given
        Long memberId = memberRepository.findByEmail(MEMBER_EMAIL).orElseThrow().getId();
        ReceivedMailKey key = receivedMailService.keyOf(memberId, message(MEMBER_EMAIL, "<race@newsletter.example>"))
                .orElseThrow();
        receivedMailService.record(key, 1L);

        // when & then
        assertThatThrownBy(() -> receivedMailService.record(key, 2L))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("동시 저장 충돌");
    }

    @Test
    void 기준_시각보다_오래된_기록만_지운다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        receivedMailRepository.insert(1L, hashOf(1), 1L, now.minusDays(8));
        receivedMailRepository.insert(1L, hashOf(2), 2L, now.minusDays(8));
        receivedMailRepository.insert(1L, hashOf(3), 3L, now.minusDays(1));

        // when
        int firstBatch = receivedMailService.deleteCreatedBefore(now.minusDays(7), 1);
        int secondBatch = receivedMailService.deleteCreatedBefore(now.minusDays(7), 1);
        int thirdBatch = receivedMailService.deleteCreatedBefore(now.minusDays(7), 1);

        // then
        assertThat(firstBatch).isOne();
        assertThat(secondBatch).isOne();
        assertThat(thirdBatch).isZero();
        assertThat(receivedMailRepository.count()).isOne();
    }

    private MimeMessage message(String to, String messageId) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        message.setFrom(new InternetAddress(NEWSLETTER_EMAIL));
        message.setSubject("테스트 이메일 제목");
        message.setText("테스트 본문");
        if (messageId != null) {
            message.setHeader("Message-ID", messageId);
        }
        return message;
    }

    private void saveMember(String email, String nickname) {
        memberRepository.save(Member.builder()
                .email(email)
                .nickname(nickname)
                .gender(Gender.MALE)
                .roleId(1L)
                .providerId(nickname)
                .provider("google")
                .build());
    }

    private byte[] hashOf(int seed) {
        byte[] hash = new byte[32];
        hash[0] = (byte) seed;
        return hash;
    }
}
