package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.YearlyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface YearlyReadingRepository extends JpaRepository<YearlyReading, Long> {

    Optional<YearlyReading> findByMemberIdAndReadingYear(Long memberId, int year);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE YearlyReading yr
        SET yr.currentCount = yr.currentCount + :monthlyCount
        WHERE yr.memberId = :memberId AND yr.readingYear = :targetYear
    """)
    int increaseMonthlyCountToYearly(@Param("memberId") Long memberId, @Param("monthlyCount") int monthlyCount, @Param("targetYear") int targetYear);
}
