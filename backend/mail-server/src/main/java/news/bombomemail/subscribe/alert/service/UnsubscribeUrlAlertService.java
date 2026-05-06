package news.bombomemail.subscribe.alert.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import news.bombomemail.common.discord.DiscordWebhookSender;
import news.bombomemail.subscribe.alert.UnsubscribeUrlFailure;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnsubscribeUrlAlertService {

    private final Map<Long, UnsubscribeUrlFailure> pendingFailures = new ConcurrentHashMap<>();
    private final DiscordWebhookSender discordWebhookSender;

    public void record(UnsubscribeUrlMissingEvent event) {
        pendingFailures.putIfAbsent(event.newsletterId(), UnsubscribeUrlFailure.from(event));
    }

    public void sendPendingAlerts() {
        List<UnsubscribeUrlFailure> failures = new ArrayList<>(pendingFailures.values());
        pendingFailures.clear();

        if (!failures.isEmpty()) {
            discordWebhookSender.sendUnsubscribeUrlMissingAlert(failures);
        }
    }
}
