package me.bombom.api.v1.article.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.repository.MarkAsReadEventLogRepository;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.TransientDataAccessResourceException;
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

    @Mock
    private MarkAsReadEventLogRepository markAsReadEventLogRepository;

    @Mock
    private DiscordWebhookNotifier discordWebhookNotifier;

    @InjectMocks
    private MarkAsReadListener markAsReadListener;

    @BeforeEach
    void setUp() {
        // 멱등성 체크는 기본적으로 신규 처리로 통과
        lenient().when(markAsReadEventLogRepository.markIfAbsent(anyLong(), anyLong()))
                .thenReturn(true);
    }

    @Test
    void 스코어를_추가할_수_있는_경우_읽기_횟수와_스코어를_증가시킨다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), true);
        given(articleService.isArrivedToday(eq(articleId), eq(memberId), any(LocalDate.class))).willReturn(true);
        given(articleService.canAddArticleScore(memberId)).willReturn(true);
        given(readingService.calculateArticleScore(memberId)).willReturn(10);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, times(1)).updateReadingCount(memberId, true);
        verify(petService, times(1)).increaseCurrentScore(memberId, 10);
    }

    @Test
    void 오늘_도착하지_않은_아티클인_경우_읽기_횟수만_갱신한다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), true);
        given(articleService.isArrivedToday(eq(articleId), eq(memberId), any(LocalDate.class))).willReturn(false);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, times(1)).updateReadingCount(memberId, false);
        verify(petService, never()).increaseCurrentScore(anyLong(), anyInt());
        verify(articleService, never()).canAddArticleScore(anyLong()); // 호출되지 않음을 확인
    }

    @Test
    void 이미_처리된_이벤트인_경우_아무_작업도_수행하지_않는다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), true);
        given(markAsReadEventLogRepository.markIfAbsent(anyLong(), anyLong()))
                .willReturn(false);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, never()).updateReadingCount(anyLong(), any(Boolean.class));
        verify(petService, never()).increaseCurrentScore(anyLong(), anyInt());
    }

    @Test
    void 오늘_도착한_아티클이지만_점수_추가_불가능한_경우_읽기_횟수만_갱신한다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), true);
        given(articleService.isArrivedToday(eq(articleId), eq(memberId), any(LocalDate.class))).willReturn(true);
        given(articleService.canAddArticleScore(memberId)).willReturn(false);

        // when
        markAsReadListener.on(event);

        // then
        verify(readingService, times(1)).updateReadingCount(memberId, true);
        verify(petService, never()).increaseCurrentScore(anyLong(), anyInt());
        verify(articleService, times(1)).canAddArticleScore(memberId); // 호출되지만 false 반환
    }

    @Test
    void 카운트_대상이_아닌_읽음_이벤트는_아무_작업도_수행하지_않는다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), false);

        // when
        markAsReadListener.on(event);

        // then
        verify(markAsReadEventLogRepository, never()).markIfAbsent(anyLong(), anyLong());
        verify(readingService, never()).updateReadingCount(anyLong(), any(Boolean.class));
        verify(petService, never()).increaseCurrentScore(anyLong(), anyInt());
    }

    @Test
    void 최종_재시도_실패_시_디스코드_알림을_보낸다() {
        // given
        Long memberId = 1L;
        Long articleId = 1L;
        MarkAsReadEvent event = MarkAsReadEvent.of(memberId, articleId, LocalDateTime.now(), true);
        TransientDataAccessResourceException exception = new TransientDataAccessResourceException("DB 일시 장애");

        // when
        markAsReadListener.recover(exception, event);

        // then
        verify(discordWebhookNotifier, times(1)).sendMarkAsReadErrorNotification(event, exception);
    }
}
