package me.bombom.api.v1.withdraw.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteReadingsByWithdrawListener {

    private final ReadingService readingService;

    @Async
    @TransactionalEventListener
    public void on(WithdrawEvent event) {
        log.info("탈퇴한 회원에 대한 읽기 정보 삭제 시작 - memberId={}", event.memberId());
        try {
            readingService.deleteAllByMemberId(event.memberId());
        } catch (Exception e) {
            log.error("탈퇴한 회원에 대한 읽기 정보 삭제 실패 - memberId = {}", event.memberId(), e);
        }
    }
}
