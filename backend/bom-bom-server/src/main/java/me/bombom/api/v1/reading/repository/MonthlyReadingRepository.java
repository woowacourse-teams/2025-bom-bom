package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReading;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlyReadingRepository extends JpaRepository<MonthlyReading, Long> {

    Optional<MonthlyReading> findByMemberId(Long memberId);

    @Query(value = """
        SELECT
            m.nickname AS nickname,
            CAST(RANK() OVER (ORDER BY mr.current_count DESC) AS INT) AS rank,
            mr.current_count AS monthlyReadCount,
            wr.current_count AS weeklyReadCount
        FROM monthly_reading mr
        JOIN member m ON mr.member_id = m.id
        JOIN weekly_reading wr ON m.id = wr.member_id
        ORDER BY mr.current_count DESC, m.nickname ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<MonthlyReadingRankResponse> findRankWithMember(@Param("limit") int limit);
}
