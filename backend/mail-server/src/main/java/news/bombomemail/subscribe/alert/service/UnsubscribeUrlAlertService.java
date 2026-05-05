package news.bombomemail.subscribe.alert.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import news.bombomemail.common.discord.DiscordWebhookSender;
import news.bombomemail.subscribe.alert.UnsubscribeUrlFailure;
import news.bombomemail.subscribe.event.UnsubscribeUrlMissingEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnsubscribeUrlAlertService {

    private final Set<Long> failedNewsletterIds = ConcurrentHashMap.newKeySet();
    private final Queue<UnsubscribeUrlFailure> pendingAlerts = new ConcurrentLinkedQueue<>();
    private final DiscordWebhookSender discordWebhookSender;

    public void record(UnsubscribeUrlMissingEvent event) {
        boolean isNew = failedNewsletterIds.add(event.newsletterId());
        if (isNew) {
            pendingAlerts.offer(UnsubscribeUrlFailure.from(event));
        }
    }

    public void drainAndSend() {
        List<UnsubscribeUrlFailure> failures = new ArrayList<>();
        UnsubscribeUrlFailure failure;
        while ((failure = pendingAlerts.poll()) != null) {
            failures.add(failure);
        }
        if (!failures.isEmpty()) {
            discordWebhookSender.sendUnsubscribeUrlMissingAlert(failures);
        }
    }
}
