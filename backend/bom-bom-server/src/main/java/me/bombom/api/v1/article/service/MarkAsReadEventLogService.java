package me.bombom.api.v1.article.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.repository.MarkAsReadEventLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkAsReadEventLogService {

    private static final int RETENTION_DAYS = 1;

    private final MarkAsReadEventLogRepository markAsReadEventLogRepository;
    private final Clock clock;

    @Transactional
    public void cleanupOldLogs() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusDays(RETENTION_DAYS);
        int deletedCount = markAsReadEventLogRepository.deleteOlderThan(threshold);
        log.info("MarkAsRead 이벤트 로그 정리 완료 - threshold={}, deleted={}", threshold, deletedCount);
    }
}
