package me.bombom.support.holiday;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.common.holiday.client.HolidayApiClient;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.support.testdouble.ResettableTestDouble;
import org.springframework.web.client.RestClient;

/**
 * 통합 테스트에서 공공데이터포털 호출 대신 연도별 공휴일 응답을 제어한다.
 */
public class FakeHolidayApiClient extends HolidayApiClient implements ResettableTestDouble {

    private final Map<Year, List<Holiday>> holidays = new HashMap<>();

    public FakeHolidayApiClient() {
        super(RestClient.builder(), null);
    }

    @Override
    public List<Holiday> getHolidays(Year year) {
        return holidays.getOrDefault(year, List.of());
    }

    public void respond(Year year, List<Holiday> holidays) {
        this.holidays.put(year, holidays);
    }

    @Override
    public void reset() {
        holidays.clear();
    }
}
