package me.bombom.api.v1.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.service.MemberService;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookNotifier {

    @Value("${discord.webhook.new_member.url}")
    private String newMemberWebhookUrl;

    @Value("${discord.webhook.unsubscribeError.url}")
    private String unsubscribeErrorWebhookUrl;

    private final WebhookHttpClient webhookClient;
    private final MemberService memberService;
    private final NewsletterService newsletterService;

    public void sendNewMemberNotification(String nickname) {
        long totalMemberCount = memberService.countNormalMembers();

        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of(
                        "title", "🎉 새로운 봄봄 회원이 가입했어요!",
                        "color", 0x00C853,
                        "fields", List.of(
                                Map.of("name", "🧑‍💻 닉네임 : ", "value", "**" + nickname + "**", "inline", true),
                                Map.of("name", "🕒 가입 시각 : ", "value", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", "inline", true),
                                Map.of("name", "🌸 현재 총 회원 수 : ", "value", totalMemberCount + "명")
                        )
                )
        ));

        webhookClient.post(newMemberWebhookUrl, body);
    }

    public void sendUnsubscribeErrorNotification(
            String message,
            Subscribe subscribe,
            String url
    ) {
        Long newsletterId = subscribe.getNewsletterId();

        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of("title", "🚨 구독 자동 취소 실패",
                        "description", message,
                        "color", 0xE74C3C,
                        "fields", List.of(
                                Map.of("name", "📰 뉴스레터", "value", getNewsletterInfo(newsletterId)),
                                Map.of("name", "🔗 해지 URL", "value", url),
                                Map.of("name", "🆔 ID 정보", "value",
                                        "Subscribe: " + subscribe.getId()
                                                + " / Member: " + subscribe.getMemberId())),
                        "timestamp", Instant.now().toString())));

        webhookClient.post(unsubscribeErrorWebhookUrl, body);
    }

    private String getNewsletterInfo(Long newsletterId) {
        String newsletterInfo;
        try {
            Newsletter newsletter = newsletterService.getNewsletter(newsletterId);
            newsletterInfo = newsletter.getName() + " (" + newsletter.getEmail() + ")";
        } catch (Exception e) {
            log.warn("구독 자동 취소 실패 알림을 보낼 뉴스레터가 없습니다. (ID: {})", newsletterId, e);
            newsletterInfo = "알 수 없음 (ID: " + newsletterId + ")";
        }
        return newsletterInfo;
    }
}
