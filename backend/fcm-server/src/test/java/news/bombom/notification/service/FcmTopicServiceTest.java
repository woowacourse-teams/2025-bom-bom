package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.List;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.repository.MemberFcmTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmTopicService 테스트")
class FcmTopicServiceTest {

    private static final String EVENT_TOPIC_NAME = "bombom_event";
    private static final Long MEMBER_ID = 1L;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private MemberFcmTokenRepository tokenRepository;

    @InjectMocks
    private FcmTopicService fcmTopicService;


    @Test
    @DisplayName("토큰 목록이 비어있으면 Firebase 호출을 하지 않는다")
    void updateSubscriptionWithTokens_EmptyTokens_NoFirebaseCall() throws Exception {
        // when
        fcmTopicService.updateSubscription(MEMBER_ID, NotificationCategory.EVENT, true, List.of());

        // then
        verify(firebaseMessaging, never()).subscribeToTopic(anyList(), eq(EVENT_TOPIC_NAME));
        verify(firebaseMessaging, never()).unsubscribeFromTopic(anyList(), eq(EVENT_TOPIC_NAME));
    }

    @Test
    @DisplayName("구독 요청 시 subscribeToTopic을 호출한다")
    void updateSubscriptionWithTokens_Subscribe_CallsSubscribe() throws Exception {
        // given
        List<String> tokens = List.of("token1", "token2");

        // when
        fcmTopicService.updateSubscription(MEMBER_ID, NotificationCategory.EVENT, true, tokens);

        // then
        verify(firebaseMessaging, times(1)).subscribeToTopic(tokens, EVENT_TOPIC_NAME);
        verify(firebaseMessaging, never()).unsubscribeFromTopic(anyList(), eq(EVENT_TOPIC_NAME));
    }

    @Test
    @DisplayName("구독 해제 요청 시 unsubscribeFromTopic을 호출한다")
    void updateSubscriptionWithTokens_Unsubscribe_CallsUnsubscribe() throws Exception {
        // given
        List<String> tokens = List.of("token1", "token2");

        // when
        fcmTopicService.updateSubscription(MEMBER_ID, NotificationCategory.EVENT, false, tokens);

        // then
        verify(firebaseMessaging, times(1)).unsubscribeFromTopic(tokens, EVENT_TOPIC_NAME);
        verify(firebaseMessaging, never()).subscribeToTopic(anyList(), eq(EVENT_TOPIC_NAME));
    }

    @Test
    @DisplayName("Firebase 예외 발생 시 예외를 전파하지 않는다 (Best effort)")
    void updateSubscriptionWithTokens_FirebaseException_DoesNotPropagate() throws Exception {
        // given
        List<String> tokens = List.of("token1");
        when(firebaseMessaging.subscribeToTopic(tokens, EVENT_TOPIC_NAME))
                .thenThrow(FirebaseMessagingException.class);

        // when & then
        assertThatCode(() ->
                fcmTopicService.updateSubscription(MEMBER_ID, NotificationCategory.EVENT, true, tokens)
        ).doesNotThrowAnyException();
    }
}

