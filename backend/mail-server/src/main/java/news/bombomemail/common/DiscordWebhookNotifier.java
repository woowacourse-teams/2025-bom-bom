package news.bombomemail.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.subscribe.alert.UnsubscribeUrlFailure;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class DiscordWebhookNotifier {

    private static final int COLOR_RED = 0xE74C3C;

    private final RestClient restClient;

    @Value("${discord.webhook.unsubscribe-alert.url}")
    private String webhookUrl;

    public DiscordWebhookNotifier(@Qualifier("discordRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUnsubscribeUrlMissingAlert(List<UnsubscribeUrlFailure> failures) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildBody(failures))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord Webhook 전송 실패: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> buildBody(List<UnsubscribeUrlFailure> failures) {
        String description = failures.stream()
                .map(f -> "📰 **%s**\n└ %s".formatted(f.newsletterName(), f.articleTitle()))
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> embed = Map.of(
                "title", "🚨 unsubscribeUrl 파싱 실패 (%d건)".formatted(failures.size()),
                "description", description,
                "color", COLOR_RED,
                "footer", Map.of("text", "매일 오후 1시 집계"),
                "timestamp", Instant.now().toString()
        );

        return Map.of("embeds", List.of(embed));
    }
}
