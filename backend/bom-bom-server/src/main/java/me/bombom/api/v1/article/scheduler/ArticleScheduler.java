package me.bombom.api.v1.article.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.article.service.PreviousArticleService;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_2AM_CRON = "0 0 2 * * *";
    private static final String DAILY_2_10_AM_CRON = "0 10 2 * * *";
    private static final String DAILY_2_20_AM_CRON = "0 20 2 * * *";
    private static final String DAILY_3AM_CRON = "0 0 3 * * *";
    private static final int MINIMUM_ARTICLE_LIMIT = 500;

    @Value("${scheduler.remove-article.max-count.admin}")
    private int adminLimit;

    @Value("${scheduler.remove-article.max-count.user}")
    private int userLimit;

    private final PreviousArticleService previousArticleService;
    private final ArticleService articleService;

    @Scheduled(cron = DAILY_2AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_previous_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldPreviousArticles() {
        log.info("이전 아티클 정리 시작");
        int deletedCount = previousArticleService.cleanupOldPreviousArticles();
        log.info("{}개 정리 완료", deletedCount);
    }

    @Scheduled(cron = DAILY_2_20_AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_recent_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void cleanupOldRecentArticles() {
        log.info("최신 아티클 정리 시작 (5일 이상 지난 데이터)");
        int deletedCount = articleService.cleanupOldRecentArticles();
        log.info("{}개 정리 완료", deletedCount);
    }

    @Scheduled(cron = DAILY_2_10_AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "move_recent_admin_articles", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void moveRecentAdminArticles() {
        log.info("어드민 아티클 복사 시작");
        previousArticleService.moveAdminArticles();
        log.info("어드민 아티클 이동 완료");
    }

    @Scheduled(cron = DAILY_3AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_excess_member_articles", lockAtLeastFor = "PT5S", lockAtMostFor = "PT10S")
    public void cleanupExcessArticles() {
        log.info("회원별 최대 아티클 수를 초과한 데이터 정리 시작");
        if(adminLimit < MINIMUM_ARTICLE_LIMIT || userLimit < MINIMUM_ARTICLE_LIMIT){
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "getArticleCountLimit");
        }
        int deletedCount = articleService.cleanupExcessArticles(adminLimit, userLimit);
        log.info("회원별 최대 아티클 수를 초과한 데이터 정리 완료: {}개", deletedCount);
    }
}
