package me.bombom.api.v1.reading.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UpdateReadingCountListenerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private ReadingService readingService;

    @Test
    void 읽기_횟수_갱신_이벤트_발행_시_관련_메서드가_호출된다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        boolean isTodayArticle = true;
        given(articleService.isArrivedToday(articleId)).willReturn(isTodayArticle);

        // when
        publisher.publishEvent(new UpdateReadingCountEvent(memberId, articleId));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(articleService, times(1)).isArrivedToday(articleId);
        verify(readingService, times(1)).updateReadingCount(memberId, isTodayArticle);
    }
}
