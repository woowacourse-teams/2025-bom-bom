package news.bombom.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.challenge.domain.ChallengeStartNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.Test;

class ChallengeStartMessageBuilderTest {

    private final ChallengeStartMessageBuilder builder = new ChallengeStartMessageBuilder();

    @Test
    void 챌린지_시작_카테고리로_메시지를_생성한다() {
        ChallengeStartNotification notification = ChallengeStartNotification.builder()
                .memberId(1L)
                .challengeId(100L)
                .challengeName("봄봄")
                .build();
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid("device-uuid")
                .fcmToken("fcm-token-value")
                .isNotificationEnabled(true)
                .build();

        NotificationMessage result = builder.build(notification, token);

        assertThat(result.getCategory()).isEqualTo(NotificationCategory.CHALLENGE_START);
    }
}
