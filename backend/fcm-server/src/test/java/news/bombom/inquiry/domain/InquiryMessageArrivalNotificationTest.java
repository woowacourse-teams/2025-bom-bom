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
    void 시도_횟수가_3회_미만이면_재시도한다() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");

        assertThat(notification.shouldRetry()).isTrue();
    }

    @Test
    @DisplayName("시도 횟수가 3회에 도달하면 재시도하지 않는다")
    void 시도_횟수가_3회에_도달하면_재시도하지_않는다() {
        InquiryMessageArrivalNotification notification = createNotification();
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");
        notification.markFailed("일시적 오류");

        assertThat(notification.shouldRetry()).isFalse();
    }

    @Test
    @DisplayName("1차 실패 시 30초 뒤로 재시도 시각을 계산한다")
    void 첫번째_실패_시_30초_뒤로_재시도_시각을_계산한다() {
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
    void 두번째_실패_시_2분_뒤로_재시도_시각을_계산한다() {
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
    void 세번째_실패_시_재시도_횟수를_소진하여_DEAD_상태가_되고_재시도_시각이_없다() {
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
