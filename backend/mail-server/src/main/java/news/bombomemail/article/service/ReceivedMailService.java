package news.bombomemail.article.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.repository.ReceivedMailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 같은 회원에게 같은 Message-ID 메일이 다시 오면 저장하지 않는다.
 * 재처리(저장 직후 앱 종료·DB 끊김)와 발신 서버의 재전송(SMTP at-least-once)을 함께 막는다.
 * Message-ID 가 없는 메일은 확인하지 않고 그대로 저장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivedMailService {

    private static final String MESSAGE_ID_HEADER = "Message-ID";
    private static final String EMPTY_MESSAGE_ID = "<>";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final ReceivedMailRepository receivedMailRepository;

    @Value("${mail.dedup.enabled:false}")
    private boolean enabled;

    /**
     * 설정을 켰는데 테이블이 아직 없으면(메인 서버 마이그레이션 전) 메일마다 저장이 실패한다.
     * 이때는 중복 확인을 끄고 메일을 그대로 받는다.
     */
    @PostConstruct
    void disableIfTableMissing() {
        if (!enabled) {
            log.info("[ReceivedMail] 중복 확인 꺼짐");
            return;
        }
        try {
            receivedMailRepository.count();
            log.info("[ReceivedMail] 중복 확인 켜짐");
        } catch (InvalidDataAccessResourceUsageException e) {
            enabled = false;
            log.error("[ReceivedMail] received_mail 테이블을 쓸 수 없어 중복 확인을 끕니다", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<ReceivedMailKey> keyOf(Long memberId, MimeMessage message) throws MessagingException {
        if (!enabled) {
            return Optional.empty();
        }
        return normalizedMessageId(message)
                .map(messageId -> new ReceivedMailKey(memberId, messageId, sha256(messageId)));
    }

    public void ensureNotReceived(ReceivedMailKey key) {
        receivedMailRepository.findArticleId(key.memberId(), key.messageIdHash())
                .ifPresent(articleId -> {
                    throw new DuplicateEmailException("이미 받은 메일: memberId=" + key.memberId()
                            + ", articleId=" + articleId + ", messageId=" + key.messageId());
                });
    }

    /**
     * 확인과 저장 사이에 다른 서버가 같은 메일을 먼저 저장하면 PK 충돌이 난다. 아티클 저장까지 함께 롤백된다.
     */
    public void record(ReceivedMailKey key, Long articleId) {
        try {
            receivedMailRepository.insert(key.memberId(), key.messageIdHash(), articleId, LocalDateTime.now());
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("동시 저장 충돌로 이미 받은 메일: memberId=" + key.memberId()
                    + ", messageId=" + key.messageId(), e);
        }
    }

    /**
     * READ COMMITTED 로 지워서 created_at 범위를 훑는 동안 넥스트키 락이 메일 저장을 막지 않게 한다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int deleteCreatedBefore(LocalDateTime cutoff, int limit) {
        return receivedMailRepository.deleteCreatedBefore(cutoff, limit);
    }

    private Optional<String> normalizedMessageId(MimeMessage message) throws MessagingException {
        String header = message.getHeader(MESSAGE_ID_HEADER, null);
        if (header == null) {
            return Optional.empty();
        }
        String messageId = MimeUtility.unfold(header).strip();
        if (messageId.isEmpty() || EMPTY_MESSAGE_ID.equals(messageId)) {
            return Optional.empty();
        }
        return Optional.of(messageId);
    }

    private byte[] sha256(String messageId) {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM).digest(messageId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " 를 지원하지 않는 JVM", e);
        }
    }

    public record ReceivedMailKey(Long memberId, String messageId, byte[] messageIdHash) {
    }

    /**
     * 이미 저장한 메일. 처리 실패와 구분해 duplicate 로 이동한다.
     */
    public static class DuplicateEmailException extends RuntimeException {

        public DuplicateEmailException(String message) {
            super(message);
        }

        public DuplicateEmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
