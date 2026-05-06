package news.bombomemail.subscribe.alert;

import java.util.List;
import lombok.RequiredArgsConstructor;
import news.bombomemail.common.DiscordWebhookNotifier;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnsubscribeUrlAlertService {

    private final PendingUnsubscribeFailures pendingFailures = new PendingUnsubscribeFailures();
    private final DiscordWebhookNotifier discordWebhookNotifier;

    public void record(UnsubscribeUrlMissingEvent event) {
        pendingFailures.record(event);
    }

    public void sendPendingAlerts() {
        List<UnsubscribeUrlFailure> failures = pendingFailures.collectForAlert();

        if (!failures.isEmpty()) {
            discordWebhookNotifier.sendUnsubscribeUrlMissingAlert(failures);
        }
    }
}
