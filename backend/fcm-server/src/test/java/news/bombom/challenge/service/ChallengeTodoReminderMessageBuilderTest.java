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
    @DisplayName("마지막 날 1차 알림 - 고정 메시지")
    void build_lastDay_first() {
        NotificationMessage msg = build(0, true, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 오늘이 마지막 날이에요! 끝까지 함께해요!");
    }

    @Test
    @DisplayName("마지막 날 2차 알림 - 고정 메시지")
    void build_lastDay_second() {
        NotificationMessage msg = build(0, true, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 마지막 날이에요. 오늘만 하면 완주예요 ⏰");
    }

    @Test
    @DisplayName("스트릭 0일 1차 알림")
    void build_streak0_first() {
        NotificationMessage msg = build(0, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("봄봄 오늘부터 함께해요. 아직 할 일이 남았어요!");
    }

    @Test
    @DisplayName("스트릭 0일 2차 알림")
    void build_streak0_second() {
        NotificationMessage msg = build(0, false, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 오늘부터 함께 습관을 쌓아봐요!");
    }

    @Test
    @DisplayName("스트릭 1일 1차 알림")
    void build_streak1_first() {
        NotificationMessage msg = build(1, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("봄봄 오늘부터 함께해요. 아직 할 일이 남았어요!");
    }

    @Test
    @DisplayName("스트릭 3일 1차 알림")
    void build_streak3_first() {
        NotificationMessage msg = build(3, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 3일 연속 기록! 오늘도 이어가요!");
    }

    @Test
    @DisplayName("스트릭 7일 1차 알림")
    void build_streak7_first() {
        NotificationMessage msg = build(7, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 벌써 7일 연속! 오늘도 같이 달려요 🏃");
    }

    @Test
    @DisplayName("스트릭 14일 1차 알림")
    void build_streak14_first() {
        NotificationMessage msg = build(14, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 14일 연속이라니! 정말 멋져요! 👍");
    }

    @Test
    @DisplayName("스트릭 21일 1차 알림")
    void build_streak21_first() {
        NotificationMessage msg = build(21, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 21일 연속! 완주가 코앞이에요. 정말 잘하고 있어요!");
    }

    @Test
    @DisplayName("스트릭 7일 2차 알림")
    void build_streak7_second() {
        NotificationMessage msg = build(7, false, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 7일 연속 기록, 오늘 놓치면 너무 아깝잖아요 🥲");
    }

    @Test
    @DisplayName("스트릭 21일 2차 알림")
    void build_streak21_second() {
        NotificationMessage msg = build(21, false, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 21일 연속, 완주까지 얼마 안 남았어요 ⏰");
    }

    @Test
    @DisplayName("탈락 위험 1차 알림 - remainingAbsences=0")
    void build_eliminationRisk_first() {
        NotificationMessage msg = build(7, false, null, 0, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 오늘 빠지면 탈락이에요. 지금 참여해요!");
        assertThat(msg.getContent()).isEqualTo("지금 참여하지 않으면 탈락이에요. 딱 5분만요! 🚨");
    }

    @Test
    @DisplayName("탈락 위험 2차 알림 - remainingAbsences=0")
    void build_eliminationRisk_second() {
        NotificationMessage msg = build(7, false, null, 0, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 오늘 참여 안 하면 탈락이에요 ⚠️");
        assertThat(msg.getContent()).isEqualTo("오늘 참여 안 하면 진짜 탈락이에요! 지금 바로 해요 ⚠️");
    }

    @Test
    @DisplayName("결석 중이어도 스트릭이 있으면 스트릭 메시지를 사용한다")
    void build_absentWithStreak_usesStreakMessage() {
        NotificationMessage msg = build(7, false, 3, 2, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 벌써 7일 연속! 오늘도 같이 달려요 🏃");
    }

    @Test
    @DisplayName("결석 중 + 스트릭 없음 1차 알림")
    void build_absentNoStreak_first() {
        NotificationMessage msg = build(0, false, 2, 3, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 2일째 결석 중이에요. 오늘은 꼭 참여해요!");
    }

    @Test
    @DisplayName("결석 중 + 스트릭 없음 2차 알림")
    void build_absentNoStreak_second() {
        NotificationMessage msg = build(0, false, 2, 3, ChallengeTodoReminderPhase.SECOND);
        assertThat(msg.getTitle()).isEqualTo("[봄봄] 2일째 결석 중이에요! 오늘부터 다시 시작하는거 어때요?");
    }

    private NotificationMessage build(int streak, boolean isLastDay, ChallengeTodoReminderPhase phase) {
        return build(streak, isLastDay, null, 5, phase);
    }

    private NotificationMessage build(int streak, boolean isLastDay, Integer daysSince, int remaining, ChallengeTodoReminderPhase phase) {
        ChallengeTodoReminderNotification notification = ChallengeTodoReminderNotification.builder()
                .memberId(1L)
                .challengeId(100L)
                .challengeName("봄봄")
                .phase(phase)
                .streak(streak)
                .daysSinceLastParticipation(daysSince)
                .remainingAbsences(remaining)
                .isLastDay(isLastDay)
                .build();
        return builder.build(notification, token());
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
