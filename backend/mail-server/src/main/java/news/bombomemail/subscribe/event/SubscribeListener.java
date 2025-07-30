package news.bombomemail.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.subscribe.service.SubscribeService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeListener {

    private final SubscribeService subscribeService;

    @TransactionalEventListener
    public void on(SubscribeEvent event) {
        try {
            subscribeService.save(event.newsletterId(), event.memberId());
        } catch (Exception e) {
            // FIXME :: 로깅 시스템 구축후 추가될 예정
            log.error("구독 리스트 저장 실패");
        }
    }
}
