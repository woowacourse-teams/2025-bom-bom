package me.bombom.api.v1.common.holiday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.support.holiday.FakeHolidayApiClient;
import me.bombom.support.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class HolidaySyncServiceTest {

    private static final Year YEAR = Year.of(2030);

    @Autowired
    private HolidaySyncService holidaySyncService;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private FakeHolidayApiClient fakeHolidayApiClient;

    @Test
    void API에서_조회한_공휴일을_저장한다() {
        // given
        fakeHolidayApiClient.respond(YEAR, List.of(
                holiday(LocalDate.of(2030, 3, 1), "삼일절"),
                holiday(LocalDate.of(2030, 3, 4), "대체공휴일(삼일절)")
        ));

        // when
        holidaySyncService.sync(YEAR);

        // then
        assertThat(holidayRepository.findAll())
                .extracting(Holiday::getDate, Holiday::getName)
                .containsExactlyInAnyOrder(
                        tuple(LocalDate.of(2030, 3, 1), "삼일절"),
                        tuple(LocalDate.of(2030, 3, 4), "대체공휴일(삼일절)")
                );
    }

    @Test
    void 이미_저장된_날짜는_이름을_갱신한다() {
        // given
        holidayRepository.save(holiday(LocalDate.of(2030, 3, 4), "삼일절 대체공휴일"));
        fakeHolidayApiClient.respond(YEAR, List.of(holiday(LocalDate.of(2030, 3, 4), "대체공휴일(삼일절)")));

        // when
        holidaySyncService.sync(YEAR);

        // then
        assertThat(holidayRepository.findAll())
                .extracting(Holiday::getDate, Holiday::getName)
                .containsExactly(tuple(LocalDate.of(2030, 3, 4), "대체공휴일(삼일절)"));
    }

    @Test
    void API에_없는_기존_공휴일은_삭제하지_않는다() {
        // given
        holidayRepository.save(holiday(LocalDate.of(2030, 6, 3), "수동 추가 휴일"));
        fakeHolidayApiClient.respond(YEAR, List.of(holiday(LocalDate.of(2030, 3, 1), "삼일절")));

        // when
        holidaySyncService.sync(YEAR);

        // then
        assertThat(holidayRepository.findAll())
                .extracting(Holiday::getDate)
                .containsExactlyInAnyOrder(LocalDate.of(2030, 3, 1), LocalDate.of(2030, 6, 3));
    }

    @Test
    void 같은_날짜의_공휴일은_이름을_합쳐_저장한다() {
        // given
        fakeHolidayApiClient.respond(YEAR, List.of(
                holiday(LocalDate.of(2030, 5, 5), "어린이날"),
                holiday(LocalDate.of(2030, 5, 5), "부처님오신날")
        ));

        // when
        holidaySyncService.sync(YEAR);

        // then
        assertThat(holidayRepository.findAll())
                .extracting(Holiday::getDate, Holiday::getName)
                .containsExactly(tuple(LocalDate.of(2030, 5, 5), "어린이날, 부처님오신날"));
    }

    private Holiday holiday(LocalDate date, String name) {
        return Holiday.builder()
                .date(date)
                .name(name)
                .build();
    }
}
