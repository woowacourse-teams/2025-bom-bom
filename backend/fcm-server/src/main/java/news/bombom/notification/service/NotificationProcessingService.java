package news.bombom.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.service.NotificationTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationProcessingService {

    private final NotificationTokenService notificationTokenService;
    private final NotificationSenderService notificationSender;
    private final NotificationStatusService statusUpdater;

    @Transactional
    public void processNotification(ArticleArrivalNotification notification) {
        List<MemberFcmToken> fcmTokens = notificationTokenService.resolveTokens(notification.getMemberId());
        
        if (fcmTokens.isEmpty()) {
            statusUpdater.markAsFailed(notification, "FCM 토큰 없음");
            return;
        }

        NotificationResultResponse result = notificationSender.sendToAllDevices(notification, fcmTokens);
        statusUpdater.updateStatus(notification, result);
    }
}
