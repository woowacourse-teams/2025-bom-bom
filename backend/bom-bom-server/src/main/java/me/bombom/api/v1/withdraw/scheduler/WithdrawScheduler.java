package me.bombom.api.v1.withdraw.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.withdraw.service.WithdrawDataCleanupService;
import me.bombom.api.v1.withdraw.service.WithdrawService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String DAILY_RECONCILE_CRON = "0 30 3 * * *";

    private final WithdrawService withdrawService;
    private final WithdrawDataCleanupService withdrawDataCleanupService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_migrate_deleted_member", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void dailyMigrateDeletedMember(){
        log.info("만료된 탈퇴 회원 정보 삭제 실행");
        withdrawService.deleteExpiredWithdrawnMembers();
    }

    /**
     * 비동기 삭제 실패 등으로 남은 고아 데이터를 정리한다.
     * 모든 삭제는 멱등하므로 이미 정리된 회원에 대해 재실행해도 안전하다.
     */
    @Scheduled(cron = DAILY_RECONCILE_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_reconcile_withdrawn_member_data", lockAtLeastFor = "PT10S", lockAtMostFor = "PT30M")
    public void reconcileWithdrawnMemberData() {
        List<Long> memberIds = withdrawService.findActiveWithdrawnMemberIds();
        log.info("탈퇴 회원 데이터 재조정 시작 - 대상 {}건", memberIds.size());
        for (Long memberId : memberIds) {
            withdrawDataCleanupService.cleanupByMemberId(memberId);
        }
        log.info("탈퇴 회원 데이터 재조정 완료");
    }
}
