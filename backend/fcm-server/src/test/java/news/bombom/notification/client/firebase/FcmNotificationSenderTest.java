package news.bombom.notification.client.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.io.IOException;
import java.util.Map;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmNotificationSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FcmNotificationSender sender;

    @Test
    void 카테고리가_있으면_analytics_label을_설정한다() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        sender.send(createMessage(NotificationCategory.ARTICLE));

        assertThat(captureSentJson()).contains("\"analytics_label\":\"ARTICLE\"");
    }

    @Test
    void 카테고리가_없으면_analytics_label을_설정하지_않는다() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        sender.send(createMessage(null));

        assertThat(captureSentJson()).doesNotContain("analytics_label");
    }

    @Test
    void 토픽_발송에도_analytics_label을_설정한다() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        sender.sendToTopic("bombom_event", "title", "body", Map.of(), NotificationCategory.EVENT);

        assertThat(captureSentJson()).contains("\"analytics_label\":\"EVENT\"");
    }

    private NotificationMessage createMessage(NotificationCategory category) {
        return NotificationMessage.builder()
                .recipient("fcm-token")
                .title("title")
                .content("body")
                .type(NotificationType.FCM)
                .category(category)
                .data(Map.of())
                .build();
    }

    private String captureSentJson() throws Exception {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(firebaseMessaging).send(captor.capture());
        return toJson(captor.getValue());
    }

    private String toJson(Message message) throws IOException {
        return GsonFactory.getDefaultInstance().toString(message);
    }
}
