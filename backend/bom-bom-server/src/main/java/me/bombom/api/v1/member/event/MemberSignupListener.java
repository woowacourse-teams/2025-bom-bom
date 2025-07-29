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
            readingService.initializeReadingInformation(event.getMemberId());
        } catch (Exception e) {
            //로깅
        }
    }
}
