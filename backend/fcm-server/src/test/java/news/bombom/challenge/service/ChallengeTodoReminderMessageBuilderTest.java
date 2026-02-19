package news.bombom.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.domain.ChallengeTodoReminderPhase;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("챌린지 TODO 리마인더 메시지 빌더 테스트")
class ChallengeTodoReminderMessageBuilderTest {

    private final ChallengeTodoReminderMessageBuilder builder = new ChallengeTodoReminderMessageBuilder();

    @Test
    @DisplayName("1차 알림이면 기본 리마인드 메시지를 생성한다")
    void build_FirstPhase() {
        ChallengeTodoReminderNotification notification = ChallengeTodoReminderNotification.builder()
                .memberId(1L)
                .challengeId(100L)
                .challengeName("러닝")
                .phase(ChallengeTodoReminderPhase.FIRST)
                .build();
        MemberFcmToken token = token();

        NotificationMessage message = builder.build(notification, token);

        assertThat(message.getTitle()).isEqualTo("러닝 아직 할 일이 남았어요!");
        assertThat(message.getContent()).isEqualTo("오늘도 기록을 채워볼까요? 지금 참여해서 꾸준하게 이어가요!");
    }

    @Test
    @DisplayName("2차 알림이면 미완료 안내 메시지를 생성한다")
    void build_SecondPhase() {
        ChallengeTodoReminderNotification notification = ChallengeTodoReminderNotification.builder()
                .memberId(1L)
                .challengeId(100L)
                .challengeName("러닝")
                .phase(ChallengeTodoReminderPhase.SECOND)
                .build();
        MemberFcmToken token = token();

        NotificationMessage message = builder.build(notification, token);

        assertThat(message.getTitle()).isEqualTo("오늘 TODO 아직 미완료예요");
        assertThat(message.getContent()).isEqualTo("출석하고 오늘 기록을 완료해 주세요.");
    }

    private MemberFcmToken token() {
        return MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid("device-1")
                .fcmToken("test-token")
                .isNotificationEnabled(true)
                .build();
    }
}
