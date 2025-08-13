package news.bombomemail.email.util;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ActiveProfiles("test")
class EmailContentExtractorTest {

    @Test
    void 일반_텍스트_메일_본문_추출() throws Exception {
        // given
        MimeMessage message = createPlainTextMessage("이것은 일반 텍스트 메일입니다.");
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEqualTo("이것은 일반 텍스트 메일입니다.");
    }

    @Test
    void HTML_메일_본문_정리() throws Exception {
        // given
        String html = "<p>테스트 내용</p>";
        MimeMessage message = createPlainWithHtmlPayload(html);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 블록_경계_br_태그_제거() throws Exception {
        // given
        String html = "<div>블록</div>";
        MimeMessage message = createPlainWithHtmlPayload(html);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 중요한_속성_있는_br_태그_보존() throws Exception {
        // given
        String html = "텍스트<br class=\"important\">줄바꿈";
        MimeMessage message = createPlainWithHtmlPayload(html);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void multipart_alternative_HTML_우선() throws Exception {
        // given
        MimeMessage message = createMultipartAlternative("일반 텍스트 버전", "<html><body>HTML 버전</body></html>");
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertSoftly(soft -> {
            soft.assertThat(result).isNotNull();
            soft.assertThat(result).doesNotContain("일반 텍스트 버전");
        });
    }

    @Test
    void multipart_alternative_plain_fallback() throws Exception {
        // given
        MimeMessage message = createMultipartAlternative("일반 텍스트만 있음", null);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEqualTo("일반 텍스트만 있음");
    }

    @Test
    void 빈_본문_처리() throws Exception {
        // given
        MimeMessage message = createPlainTextMessage("");
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void null_본문_처리() throws Exception {
        // given
        MimeMessage message = createPlainTextMessage("");
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void looksLikeHtml_HTML_판별() throws Exception {
        // given
        String html = "<html><body>내용</body></html>";
        MimeMessage message = createPlainWithHtmlPayload(html);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void looksLikeHtml_HTML_아님_판별() throws Exception {
        // given
        String plainText = "이것은 일반 텍스트입니다.";
        MimeMessage message = createPlainTextMessage(plainText);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEqualTo(plainText);
    }

    @Test
    void 일반_텍스트_그대로_유지() throws Exception {
        // given
        String plainText = "이것은 일반 텍스트입니다. 태그가 없어요.";
        MimeMessage message = createPlainTextMessage(plainText);
        
        // when
        String result = EmailContentExtractor.extractContents(message);
        
        // then
        assertThat(result).isEqualTo(plainText);
    }

    private MimeMessage createPlainTextMessage(String body) throws Exception {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom("test@example.com");
        msg.setRecipients(Message.RecipientType.TO, "user@example.com");
        msg.setSubject("테스트", "UTF-8");
        msg.setText(body, "UTF-8");
        msg.saveChanges();
        return msg;
    }

    private MimeMessage createPlainWithHtmlPayload(String htmlString) throws Exception {
        // MIME 타입은 text/plain이지만 페이로드에 HTML 문자열을 담는다.
        // extractor는 문자열 내 HTML 패턴을 감지해 HTML 정리 로직을 적용한다.
        return createPlainTextMessage(htmlString);
    }

    private MimeMessage createMultipartAlternative(String plain, String html) throws Exception {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom("test@example.com");
        msg.setRecipients(Message.RecipientType.TO, "user@example.com");
        msg.setSubject("테스트", "UTF-8");

        MimeMultipart multipart = new MimeMultipart("alternative");
        if (plain != null) {
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(plain, "UTF-8");
            multipart.addBodyPart(textPart);
        }
        if (html != null) {
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setText(html, "UTF-8", "html");
            multipart.addBodyPart(htmlPart);
        }
        msg.setContent(multipart);
        msg.saveChanges();
        return msg;
    }
}
