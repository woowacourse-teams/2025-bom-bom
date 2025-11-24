package news.bombomemail.subscribe.event;

import static org.mockito.Mockito.verify;

import news.bombomemail.article.event.ArticleArrivedEvent;
import news.bombomemail.subscribe.service.SubscribeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class SubscribeArticleArrivedListenerTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @MockitoBean
    SubscribeService subscribeService;

    @Test
    @Transactional
    void afterCommit_이벤트_발행후_서비스_호출() {
        // given
        Long newsletterId = 1L;
        String newsletterName = "테스트 뉴스레터";
        Long articleId = 1L;
        String articleTitle = "테스트 아티클";
        Long memberId = 2L;
        String unsubscribeUrl = "unsubscribeUrl";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, unsubscribeUrl));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(subscribeService).upsertSubscribe(newsletterId, memberId, unsubscribeUrl);
    }
}
