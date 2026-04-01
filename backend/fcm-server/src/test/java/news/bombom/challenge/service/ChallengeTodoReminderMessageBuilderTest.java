package news.bombom.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.domain.ChallengeTodoReminderPhase;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(ChallengeTodoReminderMessageBuilder.class)
@EnableConfigurationProperties(ReminderMessageProperties.class)
class ChallengeTodoReminderMessageBuilderTest {

    @Autowired
    private ChallengeTodoReminderMessageBuilder builder;

    @Test
    void 챌린지명이_제목에_포함된다() {
        NotificationMessage msg = build(7, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).contains("봄봄");
    }

    @Test
    void 스트릭_값이_제목에_포함된다() {
        NotificationMessage msg = build(7, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getTitle()).contains("7");
    }

    @Test
    void body가_비어있지_않다() {
        NotificationMessage msg = build(7, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getContent()).isNotBlank();
    }

    @Test
    void 탈락위험이면_일반_스트릭과_다른_메시지를_반환한다() {
        NotificationMessage risk = build(7, false, null, 0, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage normal = build(7, false, null, 3, ChallengeTodoReminderPhase.FIRST);
        assertThat(risk.getTitle()).isNotEqualTo(normal.getTitle());
        assertThat(risk.getContent()).isNotEqualTo(normal.getContent());
    }

    @Test
    void 탈락위험_body가_비어있지_않다() {
        NotificationMessage msg = build(7, false, null, 0, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getContent()).isNotBlank();
    }

    @Test
    void 마지막날이면_일반_스트릭과_다른_메시지를_반환한다() {
        NotificationMessage lastDay = build(7, true, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage normal = build(7, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(lastDay.getTitle()).isNotEqualTo(normal.getTitle());
    }

    @Test
    void 일차와_이차는_다른_제목을_반환한다() {
        NotificationMessage first = build(7, false, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage second = build(7, false, ChallengeTodoReminderPhase.SECOND);
        assertThat(first.getTitle()).isNotEqualTo(second.getTitle());
    }

    @Test
    void 스트릭_구간이_다르면_다른_제목을_반환한다() {
        NotificationMessage s3 = build(3, false, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage s7 = build(7, false, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage s14 = build(14, false, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage s21 = build(21, false, ChallengeTodoReminderPhase.FIRST);
        assertThat(s3.getTitle()).isNotEqualTo(s7.getTitle());
        assertThat(s7.getTitle()).isNotEqualTo(s14.getTitle());
        assertThat(s14.getTitle()).isNotEqualTo(s21.getTitle());
    }

    @Test
    void 결석중이면_일반_스트릭과_다른_제목을_반환한다() {
        NotificationMessage absent = build(0, false, 2, 3, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage normal = build(3, false, null, 3, ChallengeTodoReminderPhase.FIRST);
        assertThat(absent.getTitle()).isNotEqualTo(normal.getTitle());
    }

    @Test
    void 결석_body에_남은_결석_횟수가_포함된다() {
        NotificationMessage msg = build(0, false, 2, 3, ChallengeTodoReminderPhase.FIRST);
        assertThat(msg.getContent()).contains("3");
    }

    @Test
    void 결석중이어도_스트릭이_있으면_스트릭_메시지를_사용한다() {
        NotificationMessage absentWithStreak = build(7, false, 3, 2, ChallengeTodoReminderPhase.FIRST);
        NotificationMessage absentNoStreak = build(0, false, 3, 2, ChallengeTodoReminderPhase.FIRST);
        assertThat(absentWithStreak.getTitle()).isNotEqualTo(absentNoStreak.getTitle());
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
