package me.bombom.support;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.subscribe.domain.Subscribe;

public final class FakeDiscordWebhookNotifier extends DiscordWebhookNotifier implements ResettableTestDouble {

    private final List<String> newMemberNotifications = new ArrayList<>();
    private final List<UnsubscribeErrorNotification> unsubscribeErrorNotifications = new ArrayList<>();

    public FakeDiscordWebhookNotifier() {
        super(null, null, null);
    }

    @Override
    public void sendNewMemberNotification(String nickname) {
        newMemberNotifications.add(nickname);
    }

    @Override
    public void sendUnsubscribeErrorNotification(String message, Subscribe subscribe, String url) {
        unsubscribeErrorNotifications.add(new UnsubscribeErrorNotification(message, subscribe.getId(), url));
    }

    public List<String> getNewMemberNotifications() {
        return List.copyOf(newMemberNotifications);
    }

    public List<UnsubscribeErrorNotification> getUnsubscribeErrorNotifications() {
        return List.copyOf(unsubscribeErrorNotifications);
    }

    @Override
    public void reset() {
        newMemberNotifications.clear();
        unsubscribeErrorNotifications.clear();
    }

    public record UnsubscribeErrorNotification(String message, Long subscribeId, String url) {
    }
}
