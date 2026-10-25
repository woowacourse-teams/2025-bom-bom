package news.bombomemail.reading.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import news.bombomemail.reading.domain.TodayReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodayReadingRepository extends JpaRepository<TodayReading, Long> {

    Optional<TodayReading> findByMemberId(Long memberId);

    /**
     * 읽고 더해서 저장하면 동시에 도착한 아티클끼리 값을 덮어쓰므로, DB에서 한 번에 1 증가시킨다.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TodayReading todayReading
            SET todayReading.totalCount = todayReading.totalCount + 1,
                todayReading.updatedAt = :now
            WHERE todayReading.memberId = :memberId
            """)
    int increaseTotalCount(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}
