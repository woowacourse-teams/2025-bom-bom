package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupListener {

    private final ReadingService readingService;

    @TransactionalEventListener
    public void on(MemberSignupEvent event) {
        try {
            readingService.createReadingInformation(event.getMemberId());
        } catch (Exception e) {
            // TODO: 로깅 및 로직 추가
            log.error("읽기 정보 초기화에 실패했습니다.");
        }
    }
}
