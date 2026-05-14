package me.bombom.api.v1.withdraw.scheduler;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.withdraw.service.WithdrawService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 0 * * *";

    private final WithdrawService withdrawService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_migrate_deleted_member", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void dailyMigrateDeletedMember(){
        log.info("만료된 탈퇴 회원 정보 삭제 실행");
        withdrawService.deleteExpiredWithdrawnMembers();
    }
}
