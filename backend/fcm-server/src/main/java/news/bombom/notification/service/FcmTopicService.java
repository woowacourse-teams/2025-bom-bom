package news.bombom.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.repository.MemberFcmTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM 토픽 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTopicService {

    private final FirebaseMessaging firebaseMessaging;
    private final MemberFcmTokenRepository tokenRepository;

    /**
     * 토큰을 미리 조회한 후 FCM 토픽 구독/해제 (트랜잭션 내에서 토큰 조회 후 호출용)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateSubscription(Long memberId, NotificationCategory category, boolean enabled, List<String> tokens) {
        if (!category.isUseTopic()) {
            return;
        }
        processTopicOperation(memberId, category, enabled, tokens);
    }

    private void processTopicOperation(Long memberId, NotificationCategory category, boolean isSubscribe, List<String> tokens) {
        String operation = isSubscribe ? "구독" : "구독 해제";
        if (tokens.isEmpty()) {
            log.warn("토픽 {} 실패 - FCM 토큰이 없습니다. memberId={}, category={}", operation, memberId, category);
            return;
        }

        String topic = category.getTopicName();
        try {
            if (isSubscribe) {
                firebaseMessaging.subscribeToTopic(tokens, topic);
            } else {
                firebaseMessaging.unsubscribeFromTopic(tokens, topic);
            }
            log.info("토픽 {} 성공: memberId={}, category={}, topic={}, tokenCount={}", operation, memberId, category, topic, tokens.size());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 {} 실패: memberId={}, category={}, topic={}", operation, memberId, category, topic, e);
            // Best effort - 실패해도 예외 전파 안함
        }
    }
}
