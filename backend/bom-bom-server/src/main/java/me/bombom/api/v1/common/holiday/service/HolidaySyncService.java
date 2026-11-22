package me.bombom.api.v1.common.holiday.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.holiday.client.HolidayApiClient;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidaySyncService {

    private final HolidayApiClient holidayApiClient;
    private final HolidayRepository holidayRepository;

    /**
     * API에 없는 기존 공휴일(수동 추가 등)은 삭제하지 않는다.
     * 같은 날짜에 공휴일이 겹치면(예: 어린이날, 부처님오신날) 이름을 합쳐 저장한다.
     */
    @Transactional
    public void sync(Year year) {
        Map<LocalDate, String> fetchedHolidays = holidayApiClient.getHolidays(year).stream()
                .collect(Collectors.toMap(
                        Holiday::getDate,
                        Holiday::getName,
                        (first, second) -> first + ", " + second,
                        LinkedHashMap::new
                ));
        Map<LocalDate, Holiday> savedHolidays = holidayRepository
                .findAllByDateBetween(year.atDay(1), year.atMonth(12).atEndOfMonth()).stream()
                .collect(Collectors.toMap(Holiday::getDate, Function.identity()));

        fetchedHolidays.forEach((date, name) -> {
            Holiday savedHoliday = savedHolidays.get(date);
            if (savedHoliday == null) {
                holidayRepository.save(Holiday.builder()
                        .date(date)
                        .name(name)
                        .build());
                return;
            }
            savedHoliday.updateName(name);
        });
        log.info("{}년 공휴일 동기화 완료 - {}건", year, fetchedHolidays.size());
    }
}
