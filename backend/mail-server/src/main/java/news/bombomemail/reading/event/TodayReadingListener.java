package news.bombomemail.reading.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.reading.service.TodayReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodayReadingListener {

    private final TodayReadingService todayReadingService;

    @TransactionalEventListener
    public void on(TodayReadingEvent event) {
        try {
            todayReadingService.updateTodayTotalCount(event.memberId());
        } catch (Exception e) {
            // FIXME :: 로깅 시스템 구축후 추가될 예정
            log.error("오늘 읽기 totoal count 업데이트 실패");
        }
    }
}
