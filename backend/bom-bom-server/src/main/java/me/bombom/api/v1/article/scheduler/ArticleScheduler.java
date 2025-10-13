package me.bombom.api.v1.article.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_2AM_CRON = "0 0 2 * * *";

    private final ArticleService articleService;

    @Scheduled(cron = DAILY_2AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_reset_reading_count", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldPreviousArticles() {
        log.info("이전 아티클 정리 시작");
        int deletedCount = articleService.cleanupOldPreviousArticles();
        log.info("{}개 정리 완료", deletedCount);
    }
}


