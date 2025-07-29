package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

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
