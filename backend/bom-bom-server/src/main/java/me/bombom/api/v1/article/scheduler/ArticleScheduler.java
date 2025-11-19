package me.bombom.api.v1.article.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String DAILY_3AM_CRON = "0 0 3 * * *";

    private final PreviousArticleService previousArticleService;

    @Scheduled(cron = DAILY_2AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_previous_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldPreviousArticles() {
        log.info("이전 아티클 정리 시작");
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();
        log.info("{}개 정리 완료", deletedCount);
    }

    @Scheduled(cron = DAILY_3AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "move_recent_admin_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void moveRecentAdminArticles() {
        log.info("어드민 아티클 복사 시작");
        previousArticleService.moveAdminArticles();
        log.info("어드민 아티클 이동 완료");
    }
}
