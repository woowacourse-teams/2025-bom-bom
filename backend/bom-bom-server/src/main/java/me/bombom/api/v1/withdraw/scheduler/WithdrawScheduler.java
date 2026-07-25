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

    private final WithdrawService withdrawService;
    private final WithdrawDataCleanupService withdrawDataCleanupService;

    /**
     * 탈퇴 회원 데이터 유지보수를 하루 한 번 실행한다.
     * 1) 비동기 삭제 실패로 완료 표시되지 않은 고아 데이터를 재정리(멱등)한 뒤,
     * 2) 정리 완료 후 만료된 탈퇴 회원 추적 정보를 삭제한다.
     */
    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_withdrawn_member_maintenance", lockAtLeastFor = "PT10S", lockAtMostFor = "PT30M")
    public void dailyWithdrawnMemberMaintenance() {
        reconcileWithdrawnMemberData();
        log.info("만료된 탈퇴 회원 정보 삭제 실행");
        withdrawService.deleteExpiredWithdrawnMembers();
    }

    private void reconcileWithdrawnMemberData() {
        List<Long> memberIds = withdrawService.findCleanupNotCompletedMemberIds();
        log.info("탈퇴 회원 데이터 재정리 시작 - 대상 {}건", memberIds.size());
        for (Long memberId : memberIds) {
            if (withdrawDataCleanupService.cleanupByMemberId(memberId)) {
                withdrawService.completeCleanup(memberId);
            }
        }
        log.info("탈퇴 회원 데이터 재정리 완료");
    }
}
