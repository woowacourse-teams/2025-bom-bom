package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupListener {

    private final ReadingService readingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberSignupEvent event) {
        log.info("=== 이벤트 리스너 호출됨 ===");
        log.info("MemberSignupEvent 수신 - event: {}", event);
        
        if (event == null) {
            log.error("이벤트 객체가 null입니다!");
            return;
        }
        
        Long memberId = event.getMemberId();
        log.info("이벤트에서 추출한 memberId: {}", memberId);
        
        if (memberId == null) {
            log.error("회원가입 이벤트에서 memberId가 null입니다!");
            return;
        }
        
        log.info("ReadingService 초기화 메서드 호출 시작 - memberId: {}", memberId);
        try {
            readingService.initializeReadingInformation(memberId);
            log.info("ReadingService 초기화 메서드 호출 성공 - memberId: {}", memberId);
        } catch (Exception e) {
            log.error("ReadingService 초기화 중 오류 발생!");
            log.error("memberId: {}", memberId);
            log.error("에러 메시지: {}", e.getMessage());
            log.error("에러 타입: {}", e.getClass().getSimpleName());
            log.error("스택 트레이스:", e);
        }
        
        log.info("=== 이벤트 리스너 처리 완료 ===");
    }
}
