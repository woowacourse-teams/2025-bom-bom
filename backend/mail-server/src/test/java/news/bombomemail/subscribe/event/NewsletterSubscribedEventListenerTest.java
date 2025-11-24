package news.bombomemail.subscribe.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import news.bombomemail.subscribe.service.NewsletterSubscriptionCountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsletterSubscribedEventListenerTest {

    @Mock
    private NewsletterSubscriptionCountService newsletterSubscriptionCountService;

    @InjectMocks
    private NewsletterSubscriptionCountEventListener eventListener;

    @Test
    void 뉴스레터_구독_수_이벤트_정상_처리() {
        // given
        Long newsletterId = 1L;
        Long memberId = 1L;
        NewsletterSubscribedEvent event = NewsletterSubscribedEvent.of(newsletterId, memberId);

        // when
        eventListener.on(event);

        // then
        verify(newsletterSubscriptionCountService).updateNewsletterSubscriptionCount(newsletterId, memberId);
    }

    @Test
    void 서비스_예외_발생시_이벤트_리스너는_예외를_던지지_않음() {
        // given
        Long newsletterId = 1L;
        Long memberId = 1L;
        NewsletterSubscribedEvent event = NewsletterSubscribedEvent.of(newsletterId, memberId);
        
        doThrow(new RuntimeException("DB 오류")).when(newsletterSubscriptionCountService)
                .updateNewsletterSubscriptionCount(anyLong(), anyLong());

        // when & then
        assertThatCode(() -> eventListener.on(event))
                .doesNotThrowAnyException();
        
        verify(newsletterSubscriptionCountService).updateNewsletterSubscriptionCount(newsletterId, memberId);
    }
}
