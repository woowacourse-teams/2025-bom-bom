package news.bombomemail.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.service.ArticleService;
import news.bombomemail.common.logging.EmailProcessingContext;
import news.bombomemail.email.util.EmailContentExtractor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Session mailSession;
    private final ArticleService articleService;

    public void processEmailFile(File emailFile) {
        EmailProcessingContext.executeWithContext(emailFile, () -> processEmailFileInternal(emailFile));
    }
    
    private void processEmailFileInternal(File emailFile) {
        log.info("이메일 파일 처리 시작 - 크기: {} bytes", emailFile.length());
        
        try (InputStream fileInputStream = new FileInputStream(emailFile)) {
            log.debug("MimeMessage 생성 중...");
            MimeMessage mimeMessage = new MimeMessage(mailSession, fileInputStream);

            log.debug("메일 정보 - From: {}, To: {}, Subject: {}", 
                     mimeMessage.getFrom() != null ? mimeMessage.getFrom()[0] : "unknown",
                     mimeMessage.getAllRecipients() != null ? mimeMessage.getAllRecipients()[0] : "unknown",
                     mimeMessage.getSubject());
            
            log.debug("이메일 본문 추출 시작...");
            String contents = EmailContentExtractor.extractContents(mimeMessage);
            
            if (contents.trim().isEmpty()) {
                log.warn("추출된 본문이 비어있음");
            } else {
                log.debug("본문 추출 완료 - 길이: {} chars", contents.length());
            }
            
            log.debug("아티클 저장 시도...");
            boolean saved = articleService.save(mimeMessage, contents);
            
            if (saved) {
                log.info("이메일 처리 성공");
                deleteEmailFile(emailFile);
                return;
            } else {
                log.warn("아티클 저장 실패 (중복 또는 유효성 검사 실패)");
            }
        } catch (MessagingException | IOException e) {
            log.error("이메일 파싱/입출력 오류: {}", e.getMessage(), e);
        } catch (DataAccessException e) {
            log.error("DB 오류: {}", e.getMessage(), e);
            throw e; // DB 오류는 재시도를 위해 다시 던지기
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage(), e);
        }
        
        log.warn("이메일 처리 실패로 failed 디렉터리로 이동");
        moveToFailedDir(emailFile);
    }

    private void deleteEmailFile(File emailFile) {
        try {
            Files.delete(emailFile.toPath());
            log.debug("메일 파일 삭제 완료");
        } catch (IOException e) {
            log.warn("메일 파일 삭제 실패 - exists: {}, canWrite: {}, isFile: {}, Reason: {}",
                    emailFile.exists(),
                    emailFile.canWrite(),
                    emailFile.isFile(),
                    e.getMessage());
            moveToFailedDir(emailFile);
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
            log.warn("파싱 실패로 failed 디렉터리로 이동 완료: {}", target.getAbsolutePath());
            return;
        }
        log.error("failed 디렉터리로 파일 이동 실패: {}", file.getAbsolutePath());
    }
}
