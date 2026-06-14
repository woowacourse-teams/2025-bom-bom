package me.bombom.api.v1.reading.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.bombom.api.v1.reading.domain.MonthlyPeriod;
import me.bombom.api.v1.reading.dto.DailyReadCount;
import me.bombom.api.v1.reading.dto.FrequentReadNewsletter;
import me.bombom.openapi.mypage.model.ChangeDirection;
import me.bombom.openapi.mypage.model.FrequentReadNewsletterResponse;
import me.bombom.openapi.mypage.model.ReadingCalendarDayResponse;
import me.bombom.openapi.mypage.model.ReadingDashboardResponse;
import org.springframework.stereotype.Component;

@Component
public class MonthlyReportResponseMapper {

    private static final int CHANGE_RATE_SCALE = 1;

    public List<ReadingCalendarDayResponse> toReadingCalendar(
            MonthlyPeriod period,
            List<DailyReadCount> dailyReadCounts
    ) {
        Map<Integer, Long> readCountsByDay = dailyReadCounts.stream()
                .collect(Collectors.toMap(DailyReadCount::dayOfMonth, DailyReadCount::readCount));

        return period.dates()
                .map(date -> toCalendarDayResponse(date, readCountsByDay))
                .toList();
    }

    public ReadingDashboardResponse toReadingDashboard(
            long currentReadCount,
            long previousReadCount,
            long bookmarkCount,
            List<FrequentReadNewsletter> frequentReadNewsletters
    ) {
        return ReadingDashboardResponse.of(
                currentReadCount,
                calculateChangeRate(currentReadCount, previousReadCount),
                resolveChangeDirection(currentReadCount, previousReadCount),
                bookmarkCount,
                toFrequentReadNewsletterResponses(frequentReadNewsletters)
        );
    }

    private ReadingCalendarDayResponse toCalendarDayResponse(
            LocalDate date,
            Map<Integer, Long> readCountsByDay
    ) {
        long readCount = readCountsByDay.getOrDefault(date.getDayOfMonth(), 0L);
        boolean isReadDay = readCount > 0;
        return ReadingCalendarDayResponse.of(date, isReadDay, readCount);
    }

    private List<FrequentReadNewsletterResponse> toFrequentReadNewsletterResponses(List<FrequentReadNewsletter> newsletters) {
        return IntStream.range(0, newsletters.size())
                .mapToObj(index -> toFrequentReadNewsletterResponse(index, newsletters.get(index)))
                .toList();
    }

    private FrequentReadNewsletterResponse toFrequentReadNewsletterResponse(
            int index,
            FrequentReadNewsletter newsletter
    ) {
        return FrequentReadNewsletterResponse.of(
                index + 1,
                newsletter.newsletterId(),
                newsletter.name(),
                newsletter.readCount()
        );
    }

    private Double calculateChangeRate(long currentReadCount, long previousReadCount) {
        if (previousReadCount == 0) {
            return null;
        }

        return BigDecimal.valueOf(currentReadCount - previousReadCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previousReadCount), CHANGE_RATE_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ChangeDirection resolveChangeDirection(long currentReadCount, long previousReadCount) {
        if (previousReadCount == 0) {
            return null;
        }

        if (currentReadCount > previousReadCount) {
            return ChangeDirection.UP;
        } else if (currentReadCount < previousReadCount) {
            return ChangeDirection.DOWN;
        }
        return ChangeDirection.SAME;
    }
}
