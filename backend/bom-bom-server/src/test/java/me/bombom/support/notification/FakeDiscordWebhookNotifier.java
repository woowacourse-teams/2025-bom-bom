package me.bombom.support.notification;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.support.testdouble.ResettableTestDouble;

/**
 * 통합 테스트에서 Discord Webhook 호출을 막고 발송 요청 내역을 메모리에 기록한다.
 */
public final class FakeDiscordWebhookNotifier extends DiscordWebhookNotifier implements ResettableTestDouble {

    private final List<String> newMemberNotifications = new ArrayList<>();
    private final List<UnsubscribeErrorNotification> unsubscribeErrorNotifications = new ArrayList<>();
    private final List<UnresolvedInquiryRoomCounts> inquiryDailyStatusNotifications = new ArrayList<>();
    private final List<InquiryNewMessageNotification> inquiryNewMessageNotifications = new ArrayList<>();

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

    @Override
    public void sendInquiryDailyStatusNotification(UnresolvedInquiryRoomCounts statusCounts) {
        inquiryDailyStatusNotifications.add(statusCounts);
    }

    @Override
    public void sendInquiryNewMessageNotification(String content, String assigneeText) {
        inquiryNewMessageNotifications.add(new InquiryNewMessageNotification(content, assigneeText));
    }

    public List<String> getNewMemberNotifications() {
        return List.copyOf(newMemberNotifications);
    }

    public List<UnsubscribeErrorNotification> getUnsubscribeErrorNotifications() {
        return List.copyOf(unsubscribeErrorNotifications);
    }

    public List<UnresolvedInquiryRoomCounts> getInquiryDailyStatusNotifications() {
        return List.copyOf(inquiryDailyStatusNotifications);
    }

    public List<InquiryNewMessageNotification> getInquiryNewMessageNotifications() {
        return List.copyOf(inquiryNewMessageNotifications);
    }

    @Override
    public void reset() {
        newMemberNotifications.clear();
        unsubscribeErrorNotifications.clear();
        inquiryDailyStatusNotifications.clear();
        inquiryNewMessageNotifications.clear();
    }

    public record UnsubscribeErrorNotification(String message, Long subscribeId, String url) {
    }

    public record InquiryNewMessageNotification(String content, String assigneeText) {
    }
}
