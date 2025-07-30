package news.bombomemail.reading.service;

import news.bombomemail.reading.domain.TodayReading;
import news.bombomemail.reading.repository.TodayReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TodayReadingService.class)
class TodayReadingServiceTest {

    @Autowired
    TodayReadingRepository todayReadingRepository;

    @Autowired
    TodayReadingService todayReadingService;

    @Test
    void totalCount가_1_증가한다() {
        // given
        Long memberId = 1L;
        todayReadingRepository.save(TodayReading.builder()
                .memberId(memberId)
                .totalCount(0)
                .currentCount(0)
                .build());

        // when
        todayReadingService.updateTodayTotalCount(memberId);

        // then
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId).orElseThrow();
        assertThat(todayReading.getTotalCount()).isEqualTo(1);
    }

    @Test
    void TodayReading_없으면_totalCount_증가하지_않고_에러로그만_남는다() {
        // given
        Long memberId = 999L;

        // when
        todayReadingService.updateTodayTotalCount(memberId);

        // then
        assertThat(todayReadingRepository.findByMemberId(memberId)).isEmpty();
    }
} 
