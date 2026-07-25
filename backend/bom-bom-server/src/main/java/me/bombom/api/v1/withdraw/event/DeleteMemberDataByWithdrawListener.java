package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.withdraw.service.WithdrawDataCleanupService;
import me.bombom.api.v1.withdraw.service.WithdrawService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMemberDataByWithdrawListener {

    private final WithdrawDataCleanupService withdrawDataCleanupService;
    private final WithdrawService withdrawService;

    @Async
    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("회원 탈퇴에 따른 관련 데이터 삭제 시작 - memberId={}", event.memberId());
        if (withdrawDataCleanupService.cleanupByMemberId(event.memberId())) {
            withdrawService.completeCleanup(event.memberId());
        }
    }
}
