package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.service.WarningService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeWarningSettingListener {

    private final WarningService warningService;

    @TransactionalEventListener
    public void on(MemberSignupEvent event) {
        try{
            warningService.initializeWarningSetting(event.memberId());
        } catch (Exception e) {
            log.error("경고 설정 초기화에 실패했습니다. memberId: {}", event.memberId());
        }
    }
}
