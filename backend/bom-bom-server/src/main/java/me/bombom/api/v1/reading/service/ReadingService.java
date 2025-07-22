package me.bombom.api.v1.reading.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;

    @Transactional
    public void updateReadingCount(Article article){
        updateTodayReadingCount(article);
        updateWeeklyReadingCount(article);
    }

    private void updateTodayReadingCount(Article article) {
        if(article.isArrivedToday()) {
            TodayReading todayReading = todayReadingRepository.findByMemberId(article.getMemberId())
                    .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
            todayReading.increaseCurrentCount();
        }
    }

    private void updateWeeklyReadingCount(Article article) {
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(article.getMemberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.increaseCurrentCount();
    }
}

