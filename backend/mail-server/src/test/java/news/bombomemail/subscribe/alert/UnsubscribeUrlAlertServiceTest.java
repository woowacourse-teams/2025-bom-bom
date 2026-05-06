package news.bombomemail.subscribe.alert;

import news.bombomemail.common.DiscordWebhookNotifier;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnsubscribeUrlAlertServiceTest {

    @Mock
    private DiscordWebhookNotifier discordWebhookNotifier;

    @InjectMocks
    private UnsubscribeUrlAlertService unsubscribeUrlAlertService;

    @Test
    void 실패가_있으면_Discord_알림을_전송한다() {
        // given
        unsubscribeUrlAlertService.record(new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 1"));

        // when
        unsubscribeUrlAlertService.sendPendingAlerts();

        // then
        verify(discordWebhookNotifier).sendUnsubscribeUrlMissingAlert(anyList());
    }

    @Test
    void 실패가_없으면_Discord_알림을_전송하지_않는다() {
        // when
        unsubscribeUrlAlertService.sendPendingAlerts();

        // then
        verify(discordWebhookNotifier, never()).sendUnsubscribeUrlMissingAlert(anyList());
    }

    @Test
    void 알림_전송_후_다시_전송하면_Discord_알림을_전송하지_않는다() {
        // given
        unsubscribeUrlAlertService.record(new UnsubscribeUrlMissingEvent(1L, "뉴스레터A", "아티클 1"));
        unsubscribeUrlAlertService.sendPendingAlerts();

        // when
        unsubscribeUrlAlertService.sendPendingAlerts();

        // then
        verify(discordWebhookNotifier).sendUnsubscribeUrlMissingAlert(anyList()); // 정확히 1번만
    }
}
