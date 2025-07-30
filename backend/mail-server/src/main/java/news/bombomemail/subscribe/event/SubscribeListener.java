package news.bombomemail.subscribe.event;

import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.service.SubscribeService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubscribeListener {

    private final SubscribeService subscribeService;

    @TransactionalEventListener
    public void on(SubscribeEvent event) {
        try {
            subscribeService.save(event.newsletterId(), event.memberId());
        } catch (Exception e) {

        }
    }
}
