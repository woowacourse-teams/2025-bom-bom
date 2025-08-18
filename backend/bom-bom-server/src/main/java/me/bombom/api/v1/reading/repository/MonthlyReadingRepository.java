package me.bombom.api.v1.reading.repository;

import me.bombom.api.v1.reading.domain.MonthlyReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyReadingRepository extends JpaRepository<MonthlyReading, Long> {
}
