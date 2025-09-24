package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.TodayReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TodayReadingRepository extends JpaRepository<TodayReading, Long> {

    Optional<TodayReading> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE TodayReading
        SET currentCount = 0
        WHERE currentCount != 0
    """)
    void resetCurrentCount();

    @Query("""
        SELECT tr.id, tr.memberId, tr.totalCount, tr.currentCount
        FROM TodayReading tr
        WHERE tr.totalCount != 0 AND tr.currentCount = 0
    """)
    List<TodayReading> findTotalNonZeroAndCurrentZero();

    void deleteByMemberId(Long memberId);
}
