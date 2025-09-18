package me.bombom.api.v1.reading.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadingScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    public static final String EVERY_TEN_MINUTES_CRON = "0 */10 * * * *";
    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String WEEKLY_CRON = "0 0 0 * * MON";
    private static final String MONTHLY_CRON = "0 0 0 1 * ?";

    private final ReadingService readingService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_reset_reading_count", lockAtLeastFor = "4s", lockAtMostFor = "9s")
        public void dailyResetReadingCount() {
        log.info("오늘 읽기 초기화 실행");
        readingService.resetContinueReadingCount();
        readingService.resetTodayReadingCount();
    }

    @Scheduled(cron = WEEKLY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "weekly_reset_reading_count", lockAtLeastFor = "4s", lockAtMostFor = "9s")
    public void weeklyResetReadingCount() {
        log.info("주간 읽기 초기화 실행");
        readingService.resetWeeklyReadingCount();
    }

    /**
     * 매월 1일에 실행되어 지난 달의 읽기 데이터를 해당 연도의 YearlyReading에 반영
     * 예: 1월 1일 실행 시 → 12월 데이터를 2024년 YearlyReading에 추가
     *     2월 1일 실행 시 → 1월 데이터를 2025년 YearlyReading에 추가
     */
    @Scheduled(cron = MONTHLY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "monthly_reset_reading_count", lockAtLeastFor = "4s", lockAtMostFor = "9s")
    public void monthlyResetReadingCount() {
        log.info("월간 읽기를 연간 읽기에 반영 후 초기화");
        readingService.passMonthlyCountToYearly();
    }

    @Scheduled(cron = EVERY_TEN_MINUTES_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "ten_minutely_calculate_member_rank", lockAtLeastFor = "1.5s", lockAtMostFor = "3s")
    public void tenMinutelyCalculateMemberRank() {
        log.info("이달의 독서왕 순위 업데이트");
        readingService.updateMonthlyRanking();
    }
}
