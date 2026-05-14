package me.bombom.api.v1.badge.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.bombom.api.v1.badge.service.BadgeService;
import me.bombom.api.v1.reading.event.ContinueReadingIncreasedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContinueReadingIncreasedEventListenerTest {

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private ContinueReadingIncreasedEventListener continueReadingIncreasedEventListener;

    @Test
    void 연속_읽기_일수_증가_이벤트를_받으면_스트릭_뱃지를_발급한다() {
        // given
        ContinueReadingIncreasedEvent event = new ContinueReadingIncreasedEvent(1L, 7);

        // when
        continueReadingIncreasedEventListener.on(event);

        // then
        verify(badgeService, times(1)).issueStreakBadge(1L, 7);
    }
}
