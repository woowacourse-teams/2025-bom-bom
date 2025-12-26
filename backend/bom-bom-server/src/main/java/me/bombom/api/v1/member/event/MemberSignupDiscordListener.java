package me.bombom.api.v1.member.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupDiscordListener {

    private final DiscordWebhookNotifier discordNotifier;

    @Async
    @TransactionalEventListener
    public void on(MemberSignupDiscordEvent event) {
        try {
            discordNotifier.sendNewMemberNotification(event.nickname());
        } catch (Exception e) {
            log.debug("⚠️ Discord 알림 전송 실패 (무시): {}", e.getMessage());
        }
    }
}
