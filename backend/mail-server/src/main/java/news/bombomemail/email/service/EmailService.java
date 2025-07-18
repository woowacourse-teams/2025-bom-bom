package news.bombomemail.email.service;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.Article;
import news.bombomemail.article.ArticleRepository;
import news.bombomemail.member.Member;
import news.bombomemail.member.MemberRepository;
import news.bombomemail.newsletter.Newsletter;
import news.bombomemail.newsletter.NewsletterRepository;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final int WORDS_PER_MINUTE = 200;

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;

    public void processMailFile(File emlFile) {
        try (InputStream is = new FileInputStream(emlFile)) {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage msg = new MimeMessage(session, is);

            Address[] fromAddress = msg.getFrom();
            if (fromAddress == null || fromAddress.length == 0) {
                log.debug("From 주소가 없어 메일을 무시합니다: " + msg.getSubject());
                return;
            }
            Address toAddress = Arrays.stream(msg.getRecipients(RecipientType.TO))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("받는이가 존재하지 않습니다."));
            Member member = memberRepository.findByEmail(toAddress.getType())
                    .orElse(null);
            Newsletter newsletter = newsletterRepository.findByEmail(String.valueOf(Arrays.stream(fromAddress).findFirst()))
                    .orElse(null);
            String contents = extractContents(msg);

            Article article = Article.builder()
                    .title(msg.getSubject())
                    .contents(contents)
                    .expectedReadTime(calculateReadingTimeFromText(contents))
                    .contentsSummary(sliceContents(contents))
                    .memberId(0L)
                    .newsletterId(0L)
                    .arrivedDateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .build();
            articleRepository.save(article);

        } catch (Exception e) {
            // 파싱/저장 중 오류 발생 시 로그만 남기고 파일은 보존
            log.error("Failed to process mail file: {}", emlFile.getName(), e);
            moveToFailedDir(emlFile);
            return;
        }

        deleteEmailFile(emlFile);
    }

    private String extractContents(MimeMessage msg) {
        try {
            Object content = msg.getContent();

            if (content instanceof String) {
                return (String) content;
            }

            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/html")) {
                        return (String) part.getContent();
                    }
                }

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        return (String) part.getContent();
                    }
                }
            }
        } catch (IOException | MessagingException e) {
            log.warn("본문 추출 중 오류 발생: {}", e.getMessage());
        }

        return "";
    }

    private void deleteEmailFile(File emailFile) {
        try {
            Files.delete(emailFile.toPath());
            log.debug("Deleted processed mail file: {}", emailFile.getName());
        } catch (IOException e) {
            log.warn("Failed to delete processed mail file: {} - exists: {}, canWrite: {}, isFile: {}, Reason: {}",
                    emailFile.getAbsolutePath(),
                    emailFile.exists(),
                    emailFile.canWrite(),
                    emailFile.isFile(),
                    e.getMessage());
            moveToCurDir(emailFile);
        }
    }

    private void moveToFailedDir(File file) {
        File failedDir = new File(file.getParentFile().getParent(), "parsing-failed");
        if (!failedDir.exists()) {
            failedDir.mkdirs();
        }
        File target = new File(failedDir, file.getName());
        boolean success = file.renameTo(target);
        if (success) {
            log.warn("파싱 실패로 failed 디렉터리로 이동: {}", target.getAbsolutePath());
        } else {
            log.error("failed 디렉터리로 파일 이동 실패: {}", file.getAbsolutePath());
        }
    }

    private void moveToCurDir(File file) {
        File curDir = new File(file.getParentFile().getParent(), "cur");
        if (!curDir.exists()) {
            curDir.mkdirs();
        }

        String newFileName = file.getName() + ":2,S"; // Seen(읽음) 플래그
        File target = new File(curDir, newFileName);
        boolean success = file.renameTo(target);
        if (success) {
            log.warn("삭제 실패로 파일을 cur 디렉터리로 이동: {}", target.getAbsolutePath());
            return;
        }
        log.error("cur 디렉터리로 파일 이동 실패: {}", file.getAbsolutePath());
    }

    private int calculateReadingTimeFromText(String fullText) {
        if (fullText == null || fullText.trim().isEmpty()) {
            return 0;
        }
        // HTML 태그 제거 및 단어 수 세기
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
        return textOnly.length() <= 100 ? textOnly : textOnly.substring(0, 100) + "...";
    }
}
