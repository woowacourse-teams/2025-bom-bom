package me.bombom.api.v1.reading.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResult;
import me.bombom.api.v1.reading.repository.ContinueReadingDailyResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContinueReadingDailyResultService {

    /**
     * 최근 연속 읽기 기록 API는 최대 31일까지 조회한다. 여유를 두고 90일만 보관한다.
     */
    static final int RETENTION_DAYS = 90;

    private final ContinueReadingDailyResultRepository continueReadingDailyResultRepository;

    /**
     * today_reading을 초기화하기 전에 호출해야 한다. 전날의 today_reading 값으로 판정한다.
     * 기록이 실패해도 연속 읽기 초기화가 롤백되지 않도록 별도 트랜잭션으로 실행한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDailyResults(LocalDate targetDate, boolean nonWorkingDay) {
        int readCount = continueReadingDailyResultRepository.bulkInsertReadResults(targetDate);
        int noArticleCount = 0;
        if (!nonWorkingDay) {
            noArticleCount = continueReadingDailyResultRepository.bulkInsertNoArticleResults(targetDate);
        }
        int deletedCount = continueReadingDailyResultRepository.bulkDeleteBefore(
                targetDate.minusDays(RETENTION_DAYS)
        );
        log.info("연속 읽기 일별 결과 기록 완료. targetDate={}, read={}, noArticle={}, deleted={}",
                targetDate, readCount, noArticleCount, deletedCount);
    }

    public List<ContinueReadingDailyResult> findResults(Long memberId, LocalDate startDate, LocalDate endDate) {
        return continueReadingDailyResultRepository.findAllByMemberIdAndResultDateBetween(
                memberId,
                startDate,
                endDate
        );
    }

    /**
     * 일별 결과 기록이 시작된 날짜. 이 날짜부터는 기록이 연속 읽기 판정의 기준이다.
     * 보관 기간이 지난 기록을 지워도 조회 가능 기간(최대 31일)보다 항상 이르다.
     */
    public Optional<LocalDate> findRecordStartDate() {
        return continueReadingDailyResultRepository.findFirstResultDate();
    }

    @Transactional
    public void deleteByMemberId(Long memberId) {
        continueReadingDailyResultRepository.bulkDeleteByMemberId(memberId);
    }
}
