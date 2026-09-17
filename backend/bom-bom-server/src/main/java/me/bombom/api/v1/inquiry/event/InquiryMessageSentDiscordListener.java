package me.bombom.api.v1.inquiry.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberDiscordAccountRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryMessageSentDiscordListener {

    private final DiscordWebhookNotifier discordWebhookNotifier;
    private final MemberRepository memberRepository;
    private final MemberDiscordAccountRepository memberDiscordAccountRepository;

    @Async
    @TransactionalEventListener
    public void on(InquiryMessageSentEvent event) {
        try {
            String assigneeText = resolveAssignee(event.assigneeId());
            discordWebhookNotifier.sendInquiryNewMessageNotification(event.content(), assigneeText);
        } catch (Exception e) {
            log.debug("⚠️ Discord 알림 전송 실패 (무시): {}", e.getMessage());
        }
    }

    private String resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return memberDiscordAccountRepository.findByMemberId(assigneeId)
                .map(account -> "<@" + account.getDiscordId() + ">")
                .orElseGet(() -> resolveAssigneeNickname(assigneeId));
    }

    private String resolveAssigneeNickname(Long assigneeId) {
        return memberRepository.findById(assigneeId)
                .map(Member::getNickname)
                .orElse(null);
    }
}
