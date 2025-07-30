package news.bombomemail.reading.event;

import news.bombomemail.reading.service.TodayReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.verify;


@SpringBootTest
class TodayReadingListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private TodayReadingService todayReadingService;

    @Test
    @Transactional
    void afterCommit_이벤트_발행후_서비스_호출() {
        // given
        Long memberId = 1L;

        // when
        eventPublisher.publishEvent(TodayReadingEvent.from(memberId));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(todayReadingService).updateTodayTotalCount(memberId);
    }
}
