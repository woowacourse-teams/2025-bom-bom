package news.bombomemail.subscribe.event;

import news.bombomemail.subscribe.service.SubscribeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.verify;

@SpringBootTest
class SubscribeListenerTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @MockitoBean
    SubscribeService subscribeService;

    @Test
    @Transactional
    void afterCommit_이벤트_발행후_서비스_호출() {
        // given
        Long newsletterId = 1L;
        Long memberId     = 2L;
        String unsubscribeUrl = "unsubscribeUrl";

        // when
        eventPublisher.publishEvent(SubscribeEvent.of(newsletterId, memberId, unsubscribeUrl));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(subscribeService).saveOrUpdate(newsletterId, memberId, unsubscribeUrl);
    }
}
