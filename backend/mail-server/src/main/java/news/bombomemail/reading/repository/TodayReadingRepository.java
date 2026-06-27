package news.bombomemail.reading.repository;

import java.util.Optional;
import news.bombomemail.reading.domain.TodayReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayReadingRepository extends JpaRepository<TodayReading, Long> {

    Optional<TodayReading> findByMemberId(Long memberId);
}
