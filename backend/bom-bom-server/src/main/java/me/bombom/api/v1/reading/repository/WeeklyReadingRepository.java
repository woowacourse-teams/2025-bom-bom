package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WeeklyReadingRepository extends JpaRepository<WeeklyReading, Long> {

    Optional<WeeklyReading> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
            UPDATE WeeklyReading
            SET currentCount = 0
            WHERE currentCount != 0
        """
    )
    void resetCurrentCount();
}
