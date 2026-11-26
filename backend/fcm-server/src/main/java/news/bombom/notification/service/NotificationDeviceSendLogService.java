package news.bombom.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.domain.NotificationDeviceSendLog;
import news.bombom.notification.repository.NotificationDeviceSendLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDeviceSendLogService {

    private final NotificationDeviceSendLogRepository deviceSendLogRepository;

    /**
     * 발송 상태 갱신 트랜잭션과 분리해 저장한다. 로그 저장이 실패해도 알림 상태 갱신이 롤백되지 않게 하기 위함이다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAll(List<NotificationDeviceSendLog> logs) {
        deviceSendLogRepository.saveAll(logs);
    }
}
