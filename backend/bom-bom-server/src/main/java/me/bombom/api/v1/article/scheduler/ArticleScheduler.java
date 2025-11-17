package me.bombom.api.v1.article.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.repository.SearchRecentRepository;
import me.bombom.api.v1.article.service.PreviousArticleService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_2AM_CRON = "0 0 2 * * *";
    private static final int RECENT_DAYS = 3;

    private final PreviousArticleService previousArticleService;
    private final SearchRecentRepository searchRecentRepository;

    @Scheduled(cron = DAILY_2AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_previous_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldPreviousArticles() {
        log.info("이전 아티클 정리 시작");
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();
        log.info("{}개 정리 완료", deletedCount);
    }

    /**
     * 매일 새벽 2시에 search_recent에서 3일 경과분 삭제
     * B 전략: 최근 3일만 search_recent에 유지하고 나머지는 삭제
     */
    @Scheduled(cron = DAILY_2AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_search_recent", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldSearchRecent() {
        log.info("search_recent 3일 경과분 정리 시작");
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RECENT_DAYS);
            int deletedCount = searchRecentRepository.deleteOlderThan(cutoffDate);
            log.info("search_recent 정리 완료 - {}개 삭제됨", deletedCount);
        } catch (Exception e) {
            log.error("search_recent 정리 중 오류 발생", e);
        }
    }
}
