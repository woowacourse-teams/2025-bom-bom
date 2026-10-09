package news.bombom.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import news.bombom.inquiry.domain.InquiryMessage;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.repository.InquiryMessageRepository;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationPayloadType;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InquiryMessageArrivalMessageBuilderTest {

    @Mock
    private InquiryMessageRepository inquiryMessageRepository;

    @InjectMocks
    private InquiryMessageArrivalMessageBuilder builder;

    @Test
    void 문의_답변_알림을_지원한다() {
        InquiryMessageArrivalNotification notification = createNotification();

        assertThat(builder.supports(notification)).isTrue();
    }

    @Test
    void title_body_data를_포함한_알림_메시지를_생성한다() {
        InquiryMessageArrivalNotification notification = createNotification();
        InquiryMessage message = createMessage();
        MemberFcmToken token = MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid("device-uuid")
                .fcmToken("fcm-token-value")
                .isNotificationEnabled(true)
                .build();

        when(inquiryMessageRepository.findById(100L)).thenReturn(Optional.of(message));

        NotificationMessage result = builder.build(notification, token);

        assertThat(result.getRecipient()).isEqualTo("fcm-token-value");
        assertThat(result.getTitle()).isEqualTo("문의에 대한 답변이 도착했어요!");
        assertThat(result.getContent()).isEqualTo("답변 내용입니다");
        assertThat(result.getData())
                .containsEntry("roomId", "10")
                .containsEntry("notificationType", NotificationPayloadType.INQUIRY_MESSAGE_ARRIVAL);
    }

    private InquiryMessageArrivalNotification createNotification() {
        return InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(100L)
                .build();
    }

    private InquiryMessage createMessage() {
        return InquiryMessage.builder()
                .id(100L)
                .roomId(10L)
                .content("답변 내용입니다")
                .build();
    }
}
