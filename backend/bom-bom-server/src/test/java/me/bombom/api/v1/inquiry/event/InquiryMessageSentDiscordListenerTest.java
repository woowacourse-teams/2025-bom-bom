package me.bombom.api.v1.inquiry.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.MemberDiscordAccount;
import me.bombom.api.v1.member.repository.MemberDiscordAccountRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.notification.FakeDiscordWebhookNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class InquiryMessageSentDiscordListenerTest {

    @Autowired
    private InquiryMessageSentDiscordListener listener;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberDiscordAccountRepository memberDiscordAccountRepository;

    @Autowired
    private FakeDiscordWebhookNotifier fakeDiscordWebhookNotifier;

    @Test
    void 담당자가_없으면_담당자_없이_알림을_보낸다() {
        listener.on(new InquiryMessageSentEvent("문의합니다", null));

        FakeDiscordWebhookNotifier.InquiryNewMessageNotification notification = awaitFirstNotification();
        assertThat(notification.assigneeText()).isNull();
    }

    @Test
    void 디스코드_계정이_매핑되어_있으면_멘션_형식으로_알림을_보낸다() {
        Member assignee = memberRepository.save(TestFixture.normalMemberFixture());
        memberDiscordAccountRepository.save(new MemberDiscordAccount(null, assignee.getId(), "123456789"));

        listener.on(new InquiryMessageSentEvent("문의합니다", assignee.getId()));

        FakeDiscordWebhookNotifier.InquiryNewMessageNotification notification = awaitFirstNotification();
        assertThat(notification.assigneeText()).isEqualTo("<@123456789>");
    }

    @Test
    void 디스코드_계정_매핑이_없으면_닉네임으로_알림을_보낸다() {
        Member assignee = memberRepository.save(TestFixture.normalMemberFixture());

        listener.on(new InquiryMessageSentEvent("문의합니다", assignee.getId()));

        FakeDiscordWebhookNotifier.InquiryNewMessageNotification notification = awaitFirstNotification();
        assertThat(notification.assigneeText()).isEqualTo(assignee.getNickname());
    }

    @Test
    void 담당자_회원_정보를_찾을_수_없으면_담당자_없이_알림을_보낸다() {
        listener.on(new InquiryMessageSentEvent("문의합니다", 999_999L));

        FakeDiscordWebhookNotifier.InquiryNewMessageNotification notification = awaitFirstNotification();
        assertThat(notification.assigneeText()).isNull();
    }

    private FakeDiscordWebhookNotifier.InquiryNewMessageNotification awaitFirstNotification() {
        awaitUntilAsserted(() ->
                assertThat(fakeDiscordWebhookNotifier.getInquiryNewMessageNotifications()).isNotEmpty());
        List<FakeDiscordWebhookNotifier.InquiryNewMessageNotification> notifications =
                fakeDiscordWebhookNotifier.getInquiryNewMessageNotifications();
        return notifications.get(0);
    }

    private void awaitUntilAsserted(CheckedAssertion assertion) {
        AssertionError lastAssertionError = null;
        for (int i = 0; i < 20; i++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError e) {
                lastAssertionError = e;
                sleepBriefly();
            }
        }

        if (lastAssertionError != null) {
            throw lastAssertionError;
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("비동기 테스트 대기 중 인터럽트 발생", e);
        }
    }

    @FunctionalInterface
    private interface CheckedAssertion {

        void run();
    }
}
