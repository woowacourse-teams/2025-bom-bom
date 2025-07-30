package news.bombomemail.reading.service;

import news.bombomemail.reading.domain.TodayReading;
import news.bombomemail.reading.repository.TodayReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TodayReadingServiceTest {

    @Autowired
    TodayReadingRepository todayReadingRepository;

    @Autowired
    TodayReadingService todayReadingService;

    @Test
    void totalCount_증가() {
        // given
        Long memberId = 1L;
        todayReadingRepository.save(TodayReading.builder()
                .memberId(memberId)
                .totalCount(0)
                .currentCount(0)
                .build());

        // when
        todayReadingService.updateTodayTotalCount(memberId);
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId).orElseThrow();

        // then
        assertThat(todayReading.getTotalCount()).isEqualTo(1);
    }

    @Test
    void todayReading_없으면_변화없음() {
        // given
        Long memberId = 999L;

        // when
        todayReadingService.updateTodayTotalCount(memberId);
        assertThat(todayReadingRepository.findByMemberId(memberId)).isEmpty();
    }
} 
