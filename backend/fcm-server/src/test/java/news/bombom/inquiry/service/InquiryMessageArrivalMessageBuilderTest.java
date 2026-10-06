package news.bombom.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("문의 답변 알림 메시지 빌더 테스트")
class InquiryMessageArrivalMessageBuilderTest {

    private final InquiryMessageArrivalMessageBuilder builder = new InquiryMessageArrivalMessageBuilder();

    @Test
    @DisplayName("문의 답변 알림을 지원한다")
    void 문의_답변_알림을_지원한다() {
        InquiryMessageArrivalNotification notification = createNotification();

        assertThat(builder.supports(notification)).isTrue();
    }

    @Test
    @DisplayName("title, body, data를 포함한 알림 메시지를 생성한다")
    void title_body_data를_포함한_알림_메시지를_생성한다() {
        InquiryMessageArrivalNotification notification = createNotification();
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid("device-uuid")
                .fcmToken("fcm-token-value")
                .isNotificationEnabled(true)
                .build();

        NotificationMessage message = builder.build(notification, token);

        assertThat(message.getRecipient()).isEqualTo("fcm-token-value");
        assertThat(message.getTitle()).isEqualTo("문의에 대한 답변이 도착했어요!");
        assertThat(message.getContent()).isEqualTo("답변 내용입니다");
        assertThat(message.getData())
                .containsEntry("roomId", "10")
                .containsEntry("notificationType", NotificationPayloadType.INQUIRY_MESSAGE_ARRIVAL);
    }

    private InquiryMessageArrivalNotification createNotification() {
        InquiryMessageArrivalNotification notification = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(100L)
                .build();
        notification.assignRoomId(10L);
        notification.assignContent("답변 내용입니다");
        return notification;
    }
}
