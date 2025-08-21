package me.bombom.api.v1.article.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MarkAsReadListenerTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private ReadingService readingService;

    @Mock
    private PetService petService;

    @InjectMocks
    private MarkAsReadListener markAsReadListener;

    @Test
    void 스코어를_추가할_수_있는_경우_읽기_횟수와_스코어를_증가시킨다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = new MarkAsReadEvent(memberId, articleId);
        given(articleService.isArrivedToday(articleId, memberId)).willReturn(true);
        given(articleService.canAddArticleScore(memberId)).willReturn(true);
        given(readingService.calculateArticleScore(memberId)).willReturn(10);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, times(1)).updateReadingCount(memberId, true);
        verify(petService, times(1)).increaseCurrentScore(memberId, 10);
    }

    @Test
    void 스코어를_추가할_수_없는_경우_읽기_횟수만_갱신한다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = new MarkAsReadEvent(memberId, articleId);
        given(articleService.isArrivedToday(articleId, memberId)).willReturn(false);
        given(articleService.canAddArticleScore(memberId)).willReturn(false);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, times(1)).updateReadingCount(memberId, false);
        verify(petService, never()).increaseCurrentScore(anyLong(), anyInt());
    }
}
