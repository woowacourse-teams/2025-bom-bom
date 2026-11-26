package news.bombom.notification.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.NotificationOpen;
import news.bombom.notification.dto.request.NotificationOpenRequest;
import news.bombom.notification.repository.NotificationOpenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationOpenService {

    private final NotificationOpenRepository notificationOpenRepository;

    @Transactional
    public void recordOpen(NotificationOpenRequest request) {
        NotificationOpen open = NotificationOpen.builder()
                .category(request.category())
                .notificationId(request.notificationId())
                .scheduleId(request.scheduleId())
                .memberId(request.memberId())
                .deviceUuid(request.deviceUuid())
                .platform(request.platform())
                .appVersion(request.appVersion())
                .openedAt(LocalDateTime.now())
                .build();
        notificationOpenRepository.save(open);
    }
}
