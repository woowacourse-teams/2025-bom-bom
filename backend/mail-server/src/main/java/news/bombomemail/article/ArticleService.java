package news.bombomemail.article;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.member.Member;
import news.bombomemail.member.MemberRepository;
import news.bombomemail.newsletter.Newsletter;
import news.bombomemail.newsletter.NewsletterRepository;
import org.jsoup.Jsoup;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private static final int WORDS_PER_MINUTE = 200;
    private static final int MAX_SUMMARY_LENGTH = 100;

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;

    public boolean save(MimeMessage msg) throws MessagingException, IOException, DataAccessException {
        Member member = resolveMember(msg);
        if (member == null) {
            return false;
        }

        Newsletter newsletter = resolveNewsletter(msg);
        if (newsletter == null) {
            return false;
        }

        articleRepository.save(buildArticle(msg, extractContents(msg), member, newsletter));
        return true;
    }

    private Member resolveMember(MimeMessage msg) throws MessagingException {
        Address[] toRecipients = msg.getRecipients(RecipientType.TO);
        if (toRecipients == null || toRecipients.length == 0) {
            log.info("받는이가 존재하지 않아 메일을 폐기합니다: {}", msg.getSubject());
            return null;
        }

        String toEmail = ((InternetAddress) toRecipients[0]).getAddress();
        Optional<Member> optMember = memberRepository.findByEmail(toEmail);
        if (optMember.isEmpty()) {
            log.info("미등록 수신자({})라 메일을 폐기합니다: {}", toEmail, msg.getSubject());
            return null;
        }
        return optMember.get();
    }

    private Newsletter resolveNewsletter(MimeMessage msg) throws MessagingException {
        String fromEmail = Optional.ofNullable(msg.getFrom())
                .stream()
                .flatMap(Arrays::stream)
                .filter(InternetAddress.class::isInstance)
                .map(InternetAddress.class::cast)
                .map(InternetAddress::getAddress)
                .findFirst()
                .orElse(null);

        if (fromEmail == null) {
            log.info("보낸이가 존재하지 않아 메일을 폐기합니다: {}", msg.getSubject());
            return null;
        }

        Optional<Newsletter> optNewsletter = newsletterRepository.findByEmail(fromEmail);
        if (optNewsletter.isEmpty()) {
            log.info("미등록 발신자({})라 메일 폐기합니다: {}", fromEmail, msg.getSubject());
            return null;
        }
        return optNewsletter.get();
    }

    private String extractContents(MimeMessage msg) throws MessagingException, IOException {
        String text = extractTextFromPart(msg);
        return text == null ? "" : text.trim();

    }

    private String extractTextFromPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bp = multipart.getBodyPart(i);
                String result = extractTextFromPart(bp);
                if (result != null && !result.isBlank()) {
                    return result;
                }
            }
        }
        return "";
    }

    private Article buildArticle(final MimeMessage msg, final String contents, final Member member,
                                 final Newsletter newsletter) throws MessagingException {
        return Article.builder()
                .title(msg.getSubject())
                .contents(contents)
                .expectedReadTime(calculateReadingTimeFromText(contents))
                .contentsSummary(sliceContents(contents))
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .arrivedDateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

    private int calculateReadingTimeFromText(String fullText) {
        if (fullText == null || fullText.trim().isEmpty()) {
            return 0;
        }

        String textOnly = Jsoup.parse(fullText).text().trim();
        int wordCount = textOnly.split("\\s+").length;
        int minutes = (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);
        return Math.max(minutes, 1);
    }

    private String sliceContents(String contents) {
        if (contents == null || contents.isBlank()) {
            return "";
        }

        String textOnly = Jsoup.parse(contents).text(); // HTML 제거
        return textOnly.length() <= MAX_SUMMARY_LENGTH ? textOnly : textOnly.substring(0, MAX_SUMMARY_LENGTH) + "...";
    }
}
