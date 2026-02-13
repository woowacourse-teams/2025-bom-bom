package news.bombom.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationResultResponse;
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
    private final NotificationSettingService notificationSettingService;

    @Transactional
    public void processArticleArrivedNotification(ArticleArrivalNotification notification) {
        Long memberId = notification.getMemberId();
        NotificationCategory category = NotificationCategory.ARTICLE;
        if (!notificationSettingService.isEnabled(memberId, category)) {
            log.info("알림 수신 동의하지 않음: memberId={}, category={}", memberId, category);
            return;
        }

        List<MemberFcmToken> fcmTokens = notificationTokenService.resolveTokens(memberId);
        if (fcmTokens.isEmpty()) {
            statusUpdater.markAsFailed(notification, "FCM 토큰 없음");
            return;
        }

        NotificationResultResponse result = notificationSender.sendToAllDevices(notification, fcmTokens);
        statusUpdater.updateStatus(notification, result);
    }
}
