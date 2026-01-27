package news.bombomemail.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.service.ArticleService;
import news.bombomemail.email.extractor.EmailContentExtractor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Session mailSession;
    private final ArticleService articleService;

    /**
     * @throws MessagingException  이메일 파싱 실패
     * @throws IOException         파일 입출력 실패
     * @throws DataAccessException DB 처리 실패
     */
    public void processEmailFile(File inProgressFile)
            throws MessagingException, IOException, DataAccessException {

        log.info("이메일 처리 시작: {}", inProgressFile.getAbsolutePath());

        try (InputStream in = new FileInputStream(inProgressFile)) {
            MimeMessage mimeMessage = new MimeMessage(mailSession, in);
            String contents = EmailContentExtractor.extractContents(mimeMessage);

            boolean saved = articleService.save(mimeMessage, contents);
            if (!saved) {
                throw new BusinessProcessingException("아티클 저장 실패: " + inProgressFile.getName());
            }

            log.info("아티클 처리 완료: {}", inProgressFile.getAbsolutePath());
        }
    }

    public static class BusinessProcessingException extends RuntimeException {

        public BusinessProcessingException(String message) {
            super(message);
        }
    }
}
