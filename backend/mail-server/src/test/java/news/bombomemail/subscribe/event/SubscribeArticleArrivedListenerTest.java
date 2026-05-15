package news.bombomemail.subscribe.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import news.bombomemail.article.event.ArticleArrivedEvent;
import news.bombomemail.article.event.ArticleSource;
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
    void afterCommit_이벤트_발행후_서비스_호출() throws Exception {
        // given
        Long newsletterId = 1L;
        String newsletterName = "테스트 뉴스레터";
        Long articleId = 1L;
        String articleTitle = "테스트 아티클";
        Long memberId = 2L;
        String unsubscribeUrl = "unsubscribeUrl";
        String contents = "테스트 본문";
        
        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, unsubscribeUrl, contents,
                ArticleSource.EMAIL_RECEIVED));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(subscribeService).upsertSubscribe(newsletterId, memberId, unsubscribeUrl, newsletterName, articleTitle);
    }

    @Test
    @Transactional
    void 매일메일_발행_이벤트는_구독_저장을_호출하지_않는다() {
        // given
        Long newsletterId = 1L;
        String newsletterName = "매일메일";
        Long articleId = 1L;
        String articleTitle = "매일메일 아티클";
        Long memberId = 2L;
        String contents = "매일메일 본문";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, null, contents,
                ArticleSource.MAEIL_MAIL_ISSUED));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verifyNoInteractions(subscribeService);
    }
}
