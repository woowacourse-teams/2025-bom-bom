package news.bombomemail.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    private static final String DIR_IN_PROGRESS = "in-progress";
    private static final String DIR_PROCESSED = "processed";
    private static final String DIR_FAILED = "parsing-failed";

    private final Session mailSession;
    private final ArticleService articleService;

    public void processEmailFile(File emailFile) {
        File inprogressFile = claimToInProgress(emailFile);
        if (inprogressFile == null) {
            return;
        }

        try {
            Result result = processInProgressFile(inprogressFile);

            String targetDir = (result == Result.PROCESSED) ? DIR_PROCESSED : DIR_FAILED;
            File moved = move(inprogressFile, targetDir);
            if (moved == null) {
                log.error("{} 이동 실패: {}", targetDir, inprogressFile.getAbsolutePath());
            }
        } catch (MessagingException | IOException e) {
            handleFailure(inprogressFile, "이메일 파싱/입출력 오류", e);
        } catch (DataAccessException e) {
            handleFailure(inprogressFile, "DB 오류", e);
        } catch (Exception e) {
            handleFailure(inprogressFile, "예상치 못한 오류", e);
        }
    }

    private Result processInProgressFile(File inprogressFile) throws MessagingException, IOException {
        try (InputStream in = new FileInputStream(inprogressFile)) {
            MimeMessage mimeMessage = new MimeMessage(mailSession, in);
            String contents = EmailContentExtractor.extractContents(mimeMessage);

            boolean saved = articleService.save(mimeMessage, contents);
            return saved ? Result.PROCESSED : Result.FAILED;
        }
    }

    private File claimToInProgress(File newFile) {
        File moved = move(newFile, DIR_IN_PROGRESS);
        if (moved == null) {
            return null;
        }

        // stale 판정을 위해 "처리 시작 시점"으로 갱신
        boolean touched = moved.setLastModified(System.currentTimeMillis());
        if (!touched) {
            log.debug("lastModified 갱신 실패(무시 가능): {}", moved.getAbsolutePath());
        }
        return moved;
    }

    private File move(File file, String dirName) {
        File baseDir = file.getParentFile().getParentFile();
        File dir = new File(baseDir, dirName);

        if (!dir.exists() && !dir.mkdirs()) {
            log.error("{} 디렉터리 생성 실패: {}", dirName, dir.getAbsolutePath());
            return null;
        }

        File target = new File(dir, file.getName());

        if (file.renameTo(target)) {
            log.info("{}로 이동: {}", dirName, target.getAbsolutePath());
            return target;
        }

        try {
            Files.copy(
                    file.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            if (!file.delete()) {
                log.error("원본 삭제 실패(중복 위험): {}", file.getAbsolutePath());
            }

            log.info("{}로 복사+삭제 이동: {}", dirName, target.getAbsolutePath());
            return target;

        } catch (Exception e) {
            log.error("{}로 이동 실패: {}", dirName, file.getAbsolutePath(), e);
            return null;
        }
    }

    private void handleFailure(File inprogressFile, String reason, Exception e) {
        if (inprogressFile.getParentFile().getName().equals(DIR_FAILED)) {
            log.warn("이미 failed에 있음: {}", inprogressFile.getAbsolutePath());
            return;
        }

        if (e == null) {
            log.warn("{}: {}", reason, inprogressFile.getAbsolutePath());
        } else {
            log.error("{}: {}", reason, inprogressFile.getName(), e);
        }

        File moved = move(inprogressFile, DIR_FAILED);
        if (moved == null) {
            log.error("failed 이동 실패: {}", inprogressFile.getAbsolutePath());
        }
    }

    private enum Result {PROCESSED, FAILED}
}
