package me.bombom.api.v1.reading.scheduler;

import java.time.LocalDateTime;
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
    @SchedulerLock(name = "daily_reset_reading_count", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
        public void dailyResetReadingCount() {
        log.info("오늘 읽기 초기화 실행");
        readingService.resetContinueReadingCount();
        readingService.resetTodayReadingCount();
    }

    @Scheduled(cron = WEEKLY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "weekly_reset_reading_count", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
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
    @SchedulerLock(name = "monthly_reset_reading_count", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void monthlyResetReadingCount() {
        log.info("월간 읽기를 연간 읽기에 반영 후 초기화");
        readingService.migrateMonthlyCountToYearlyAndReset();

        // 초기화 직후에는 모든 카운트가 0이므로 랭킹 업데이트는 의미 없음
        // (10분 후부터 10분마다 업데이트 스케줄러에서 처리됨)
        log.info("초기화 완료 - 10분 후부터 랭킹 업데이트 시작됨");
    }

    @Scheduled(cron = EVERY_TEN_MINUTES_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "ten_minutely_calculate_member_rank", lockAtLeastFor = "PT3S", lockAtMostFor = "PT15S")
    public void tenMinutelyCalculateMemberRank() {
        log.info("이달의 독서왕 순위 업데이트");

        // 매월 1일 00:00~00:10분 사이에는 월간 초기화 작업이 진행 중이므로 랭킹 업데이트 스킵
        // (초기화 직후라서 모든 카운트가 0이므로 의미 있는 랭킹 계산 불가능)
        if (shouldSkipRankingUpdate()) {
            log.info("매월 1일 00:00~00:10분 - 초기화 직후라서 모든 카운트가 0이므로 랭킹 업데이트 스킵");
            return;
        }

        readingService.updateMonthlyRanking();
        log.info("이달의 독서왕 순위 업데이트 완료");
    }

    /**
     * 매월 1일 00:00~00:09분 사이인지 확인
     * 초기화 직후에는 랭킹 업데이트를 스킵하기 위해 사용
     *
     * 이유: 00:00에 초기화되면 00:10분까지는 데이터가 없음
     * - 00:00~00:09분: 스킵 (데이터 없음)
     * - 00:10분부터: 업데이트 (10분간 데이터 쌓임)
     */
    private boolean shouldSkipRankingUpdate() {
        LocalDateTime now = LocalDateTime.now();

        // 매월 1일이고 시간이 00:00~00:09분 사이인 경우 스킵
        if (now.getDayOfMonth() == 1) {
            int hour = now.getHour();
            int minute = now.getMinute();

            // 00:00~00:09분 사이: 초기화 직후라서 데이터가 없음
            if (hour == 0 && minute <= 9) {
                log.info("매월 1일 00:{}분 - 초기화 직후라서 데이터가 없으므로 랭킹 업데이트 스킵", minute);
                return true;
            }
        }

        return false;
    }
}
