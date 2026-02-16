package news.bombom.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.repository.EventNotificationScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventNotificationScheduleRepository scheduleRepository;

    public List<EventNotificationSchedule> getPendingSchedules() {
        return scheduleRepository.findPendingSchedules(LocalDateTime.now());
    }
}
