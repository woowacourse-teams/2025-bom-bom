package news.bombomemail.reading.event;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import news.bombomemail.article.event.ArticleArrivedEvent;
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
    void afterCommit_이벤트_발행후_서비스_호출() throws Exception {
        // given
        Long newsletterId = 1L;
        String newsletterName = "테스트 뉴스레터";
        Long articleId = 1L;
        String articleTitle = "테스트 아티클";
        Long memberId = 1L;
        String unsubscribeUrl = "unsubscribeUrl";
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        String contents = "테스트 본문";

        // when
        eventPublisher.publishEvent(ArticleArrivedEvent.of(
                newsletterId, newsletterName, articleId, articleTitle, memberId, unsubscribeUrl, message, contents));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(todayReadingService).updateTodayTotalCount(memberId);
    }
}
