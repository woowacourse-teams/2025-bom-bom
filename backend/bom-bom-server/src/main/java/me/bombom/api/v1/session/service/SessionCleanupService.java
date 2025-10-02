package me.bombom.api.v1.session.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.session.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 정리 서비스
 * Spring Session 테이블에서 만료된 세션을 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionCleanupService {

    private final SessionRepository sessionRepository;

    /**
     * 만료된 세션 정리
     * EXPIRY_TIME이 현재 시간보다 이전인 세션들을 삭제

     * @return 삭제된 세션 수
     */
    @Transactional
    public int cleanupExpiredSessions() {
        long currentTimeMillis = System.currentTimeMillis();
        
        int deletedCount = sessionRepository.deleteExpiredSessions(currentTimeMillis);
        
        if (deletedCount > 0) {
            log.info("만료된 세션 정리 완료 - 삭제된 세션 수: {}", deletedCount);
        } else {
            log.debug("정리할 만료된 세션이 없습니다");
        }

        return deletedCount;
    }

    /**
     * 철저한 세션 정리
     * 만료된 세션뿐만 아니라 오래된 세션들도 함께 정리
     * 
     * @return 삭제된 세션 수
     */
    @Transactional
    public int cleanupExpiredSessionsCompletely() {
        long currentTimeMillis = System.currentTimeMillis();
        // 추가로 30일 이상 된 세션도 정리
        long thirtyDaysAgoMillis = currentTimeMillis - 30L * 24 * 60 * 60 * 1000;
        
        int deletedCount = sessionRepository.deleteExpiredAndOldSessions(currentTimeMillis, thirtyDaysAgoMillis);
        
        if (deletedCount > 0) {
            log.info("철저한 세션 정리 완료 - 삭제된 세션 수: {}", deletedCount);
        } else {
            log.debug("정리할 세션이 없습니다");
        }
        return deletedCount;
    }

    /**
     * 현재 활성 세션 수 조회
     * 모니터링 목적으로 사용
     * 
     * @return 현재 세션 수
     */
    public int getTotalSessionCount() {
        return sessionRepository.countTotalSessions();
    }

    /**
     * 만료된 세션 수 조회
     * 
     * @return 만료된 세션 수
     */
    public int getExpiredSessionCount() {
        long currentTimeMillis = System.currentTimeMillis();
        return sessionRepository.countExpiredSessions(currentTimeMillis);
    }
}
