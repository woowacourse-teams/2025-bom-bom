package news.bombom.inquiry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import news.bombom.notification.domain.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("문의 답변 알림 재시도 정책 테스트")
class InquiryMessageArrivalNotificationTest {

    @Test
    @DisplayName("시도 횟수가 3회 미만이면 재시도한다")
    void shouldRetry_UnderMaxAttempts_ReturnsTrue() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");

        assertThat(notification.shouldRetry()).isTrue();
    }

    @Test
    @DisplayName("시도 횟수가 3회에 도달하면 재시도하지 않는다")
    void shouldRetry_ReachedMaxAttempts_ReturnsFalse() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");

        assertThat(notification.shouldRetry()).isFalse();
    }

    @Test
    @DisplayName("1차 실패 시 30초 뒤로 재시도 시각을 계산한다")
    void calculateNextRetryTime_FirstAttempt_30Seconds() {
        InquiryMessageArrivalNotification notification = createNotification();

        LocalDateTime before = LocalDateTime.now();
        notification.markFailed("일시적 오류");
        LocalDateTime after = LocalDateTime.now();

        assertThat(notification.getNextRetryAt())
                .isAfterOrEqualTo(before.plusSeconds(30))
                .isBeforeOrEqualTo(after.plusSeconds(30));
    }

    @Test
    @DisplayName("2차 실패 시 2분 뒤로 재시도 시각을 계산한다")
    void calculateNextRetryTime_SecondAttempt_2Minutes() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");

        LocalDateTime before = LocalDateTime.now();
        notification.markFailed("일시적 오류");
        LocalDateTime after = LocalDateTime.now();

        assertThat(notification.getNextRetryAt())
                .isAfterOrEqualTo(before.plusMinutes(2))
                .isBeforeOrEqualTo(after.plusMinutes(2));
    }

    @Test
    @DisplayName("3차 실패 시 재시도 횟수를 소진하여 DEAD 상태가 되고 재시도 시각이 없다")
    void markFailed_ThirdAttempt_BecomesDead() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");

        notification.markFailed("일시적 오류");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DEAD);
        assertThat(notification.getNextRetryAt()).isNull();
    }

    private InquiryMessageArrivalNotification createNotification() {
        return InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(100L)
                .build();
    }
}
