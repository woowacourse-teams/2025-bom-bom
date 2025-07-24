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
import news.bombomemail.article.ArticleService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Session mailSession;
    private final ArticleService articleService;

    public void processEmailFile(File emailFile) {
        try (InputStream fileInputStream = new FileInputStream(emailFile)) {
            MimeMessage mimeMessage = new MimeMessage(mailSession, fileInputStream);
            boolean saved = articleService.save(mimeMessage);
            if (saved) {
                deleteEmailFile(emailFile);
                return;
            }
            moveToFailedDir(emailFile);
        } catch (MessagingException | IOException e) {
            log.error("이메일 파싱/입출력 오류: {}", emailFile.getName(), e);
            moveToFailedDir(emailFile);
        } catch (DataAccessException e) {
            log.error("DB 오류: {}", emailFile.getName(), e);
            moveToFailedDir(emailFile);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", emailFile.getName(), e);
            moveToFailedDir(emailFile);
        }
    }

    private void deleteEmailFile(File emailFile) {
        try {
            Files.delete(emailFile.toPath());
            log.debug("메일 삭제 mail file: {}", emailFile.getName());
        } catch (IOException e) {
            log.warn("메일을 삭제하는데 실패했습니다. mail file: {} - exists: {}, canWrite: {}, isFile: {}, Reason: {}",
                    emailFile.getAbsolutePath(),
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
            log.warn("파싱 실패로 failed 디렉터리로 이동: {}", target.getAbsolutePath());
            return;
        }
        log.error("failed 디렉터리로 파일 이동 실패: {}", file.getAbsolutePath());
    }
}
