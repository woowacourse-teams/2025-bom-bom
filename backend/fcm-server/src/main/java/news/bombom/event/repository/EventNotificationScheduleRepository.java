package news.bombom.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.event.domain.EventNotificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventNotificationScheduleRepository extends JpaRepository<EventNotificationSchedule, Long> {

    @Query("""
       SELECT s
       FROM EventNotificationSchedule s
       JOIN Event e ON s.eventId = e.id
       WHERE s.scheduledAt <= :now
         AND s.sent = false
         AND e.status = 'SCHEDULED'
       ORDER BY s.scheduledAt ASC
   """)
    List<EventNotificationSchedule> findPendingSchedules(@Param("now") LocalDateTime now);
}
