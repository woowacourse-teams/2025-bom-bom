package news.bombomemail.reading.event;

import lombok.RequiredArgsConstructor;
import news.bombomemail.reading.service.TodayReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TodayReadingListener {

    private final TodayReadingService todayReadingService;

    @TransactionalEventListener
    public void on(TodayReadingEvent event) {
        try {
            todayReadingService.updateTodayTotalCount(event.memberId());
        } catch (Exception e) {

        }
    }
}
