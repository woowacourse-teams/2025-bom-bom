package news.bombomemail.reading.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.reading.repository.TodayReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayReadingService {

    private final TodayReadingRepository todayReadingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTodayTotalCount(Long memberId) {
        int updatedCount = todayReadingRepository.increaseTotalCount(memberId, LocalDateTime.now());
        if (updatedCount == 0) {
            log.error("해당 유저의 TodayReading이 존재하지 않습니다. memberId={}", memberId);
        }
    }
}
