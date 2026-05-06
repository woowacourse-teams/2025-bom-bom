package news.bombomemail.subscribe.alert;

import lombok.RequiredArgsConstructor;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UnsubscribeUrlMissingEventListener {

    private final UnsubscribeUrlAlertService unsubscribeUrlAlertService;

    @TransactionalEventListener
    public void on(UnsubscribeUrlMissingEvent event) {
        unsubscribeUrlAlertService.record(event);
    }
}
