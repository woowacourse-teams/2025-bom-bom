package me.bombom.api.v1.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
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

    @Value("${discord.webhook.operationError.url}")
    private String operationErrorWebhookUrl;

    @Value("${discord.webhook.inquiry.url}")
    private String inquiryWebhookUrl;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

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
                                Map.of("name", "🕒 가입 시각 : ", "value",
                                        "<t:" + (System.currentTimeMillis() / 1000) + ":F>", "inline", true),
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

        webhookClient.post(operationErrorWebhookUrl, body);
    }

    public void sendMarkAsReadErrorNotification(MarkAsReadEvent event, Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of("title", "🚨 읽기 카운트 처리 최종 실패",
                        "description", message,
                        "color", 0xE74C3C,
                        "fields", List.of(
                                Map.of("name", "👤 멤버 ID", "value", String.valueOf(event.memberId()), "inline", true),
                                Map.of("name", "📰 아티클 ID", "value", String.valueOf(event.articleId()), "inline", true),
                                Map.of("name", "🕒 읽은 시각", "value", String.valueOf(event.readAt()))
                        ),
                        "timestamp", Instant.now().toString())));

        webhookClient.post(operationErrorWebhookUrl, body);
    }

    public void sendInquiryDailyStatusNotification(UnresolvedInquiryRoomCounts statusCounts) {
        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of(
                        "title", withEnvironmentPrefix("📊 오늘의 문의 현황"),
                        "color", 0x3498DB,
                        "fields", List.of(
                                Map.of("name", "🆕 미확인", "value", statusCounts.unconfirmedCount() + "건", "inline", true),
                                Map.of("name", "🔄 진행중", "value", statusCounts.inProgressCount() + "건", "inline", true)
                        ),
                        "timestamp", Instant.now().toString())));

        webhookClient.post(inquiryWebhookUrl, body);
    }

    public void sendInquiryNewMessageNotification(String content, String assigneeNickname) {
        String assigneeText = assigneeNickname == null ? "담당자 조회 실패" : assigneeNickname;

        Map<String, Object> body = Map.of("embeds", List.of(
                Map.of(
                        "title", withEnvironmentPrefix("💬 새로운 문의 메시지가 도착했어요"),
                        "color", 0xF1C40F,
                        "fields", List.of(
                                Map.of("name", "🏷️ 담당자", "value", assigneeText, "inline", true),
                                Map.of("name", "📝 내용", "value", content.isBlank() ? "(이미지)" : content)
                        ),
                        "timestamp", Instant.now().toString())));

        webhookClient.post(inquiryWebhookUrl, body);
    }

    private String withEnvironmentPrefix(String title) {
        if ("prod".equals(activeProfile)) {
            return title;
        }
        return "[" + activeProfile.toUpperCase() + "] " + title;
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
