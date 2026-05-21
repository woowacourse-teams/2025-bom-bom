package me.bombom.api.v1.common.holiday.repository;

import java.time.LocalDate;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByDate(LocalDate date);
}
