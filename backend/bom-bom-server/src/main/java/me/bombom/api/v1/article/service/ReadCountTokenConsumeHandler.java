package me.bombom.api.v1.article.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadRateLimitService;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadCountTokenConsumeHandler {

    private final ReadRateLimitService readRateLimitService;

    public boolean consume(Long memberId, Long articleId, LocalDateTime readAt) {
        try {
            return readRateLimitService.tryConsumeReadCountToken(memberId, readAt);
        } catch (TransientDataAccessException e) {
            log.error("읽기 토큰 소비 실패 - countable=true로 처리합니다. memberId={}, articleId={}, readAt={}",
                    memberId, articleId, readAt, e);
            return true;
        }
    }
}
