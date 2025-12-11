package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.service.WarningService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteWarningSettingByWithdrawListener {

    private final WarningService warningService;

    @Async
    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("탈퇴한 회원에 대한 경고 설정 삭제 시작 - memberId={}", event.memberId());
        try {
            warningService.deleteByMemberId(event.memberId());
        } catch (Exception e) {
            log.error("탈퇴한 회원에 대한 경고 설정 삭제 실패 - memberId={}", event.memberId(), e);
        }
    }
}
