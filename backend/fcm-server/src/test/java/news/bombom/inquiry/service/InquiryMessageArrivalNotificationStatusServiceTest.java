package news.bombom.inquiry.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.inquiry.repository.InquiryMessageArrivalNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InquiryMessageArrivalNotificationStatusServiceTest {

    @Mock
    private InquiryMessageArrivalNotificationRepository notificationRepository;

    @InjectMocks
    private InquiryMessageArrivalNotificationStatusService statusService;

    @Test
    void 알림_수신_거부_시_알림을_삭제한다() {
        InquiryMessageArrivalNotification notification = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(10L)
                .build();
        ReflectionTestUtils.setField(notification, "id", 1L);

        statusService.handleRejected(notification);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void 알림_수신_거부_시_상태를_변경하지_않는다() {
        InquiryMessageArrivalNotification notification = InquiryMessageArrivalNotification.builder()
                .memberId(1L)
                .messageId(10L)
                .build();
        ReflectionTestUtils.setField(notification, "id", 1L);

        statusService.handleRejected(notification);

        verify(notificationRepository, never()).save(any(InquiryMessageArrivalNotification.class));
    }
}
