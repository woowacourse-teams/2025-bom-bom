package news.bombomemail.reading.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import news.bombomemail.reading.domain.TodayReading;
import news.bombomemail.reading.repository.TodayReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayReadingService {

    private final TodayReadingRepository todayReadingRepository;

    @Transactional
    public void updateTodayTotalCount(Long memberId) {
            todayReadingRepository.findByMemberId(memberId)
                    .ifPresentOrElse(
                            TodayReading::increaseTotalCount,
                            () -> log.error("해당 유저의 TodayReading이 존재하지 않습니다. memberId={}", memberId)
                    );
    }
}
