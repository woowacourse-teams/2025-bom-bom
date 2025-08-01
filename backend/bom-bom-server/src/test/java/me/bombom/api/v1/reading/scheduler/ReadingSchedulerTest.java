package me.bombom.api.v1.reading.scheduler;

import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReadingSchedulerTest {

    @InjectMocks
    private ReadingScheduler readingScheduler;

    @Mock
    private ReadingService readingService;

    @Test
    void daily메서드는_매일_5시에_실행된다() {
        // when
        readingScheduler.daily();

        // then
        verify(readingService, times(1)).resetTodayReadingCount();
    }

    @Test
    void weekly메서드는_매주_월요일_5시에_실행된다() {
        // when
        readingScheduler.weekly();

        // then
        verify(readingService, times(1)).resetWeeklyReadingCount();
    }
}
